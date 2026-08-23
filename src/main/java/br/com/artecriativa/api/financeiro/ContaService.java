package br.com.artecriativa.api.financeiro;

import br.com.artecriativa.api.common.RecursoNaoEncontradoException;
import br.com.artecriativa.api.financeiro.dto.ContaParceladaRequest;
import br.com.artecriativa.api.financeiro.dto.ContaRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Contas a pagar/receber. Diferente de {@code Venda}/{@code MateriaPrimaService}, o
 * dinheiro só sai/entra de verdade quando a conta é <b>marcada como paga</b> — criar uma
 * conta é só um lembrete de vencimento, não é um lançamento financeiro. Por isso
 * {@link #marcarComoPaga} (e {@link #atualizar}/{@link #excluir} numa conta já paga)
 * são os únicos pontos que mexem em {@link LancamentoFinanceiro}, via
 * {@link #sincronizarLancamento}.
 */
@Service
@RequiredArgsConstructor
public class ContaService {

    private final ContaRepository contaRepository;
    private final LancamentoFinanceiroRepository lancamentoFinanceiroRepository;

    @Transactional(readOnly = true)
    public List<Conta> listarTodas() {
        return contaRepository.findAllByOrderByVencimento();
    }

    @Transactional(readOnly = true)
    public List<Conta> listarPorTipo(TipoConta tipo) {
        return contaRepository.findByTipoOrderByVencimento(tipo);
    }

    @Transactional(readOnly = true)
    public Conta buscarPorId(Long id) {
        return contaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não encontrada: " + id));
    }

    @Transactional
    public Conta criar(ContaRequest request) {
        Conta conta = new Conta();
        aplicarRequest(conta, request);
        return contaRepository.save(conta);
    }

    /**
     * Registra uma conta parcelada de uma vez: gera {@code quantidadeParcelas} contas
     * independentes, uma por mês a partir de {@code primeiroVencimento}, cada uma já
     * PENDENTE e paga/editável/excluível sozinha dali em diante — não existe uma
     * entidade "parcelamento", só o {@code grupoParcelamentoId} compartilhado pra
     * identificar que vieram da mesma compra. Divide {@code valorTotal} igualmente e
     * joga o resto do arredondamento na última parcela, pra soma bater exatamente com
     * o valor total informado.
     */
    @Transactional
    public List<Conta> criarParcelada(ContaParceladaRequest request) {
        int quantidade = request.quantidadeParcelas();
        BigDecimal valorParcela = request.valorTotal().divide(BigDecimal.valueOf(quantidade), 2, RoundingMode.DOWN);
        BigDecimal restoNaUltima = request.valorTotal().subtract(valorParcela.multiply(BigDecimal.valueOf(quantidade)));

        UUID grupoId = UUID.randomUUID();
        List<Conta> parcelas = new ArrayList<>();
        for (int i = 1; i <= quantidade; i++) {
            Conta conta = new Conta();
            conta.setTipo(request.tipo());
            conta.setDescricao("%s (parcela %d/%d)".formatted(request.descricao(), i, quantidade));
            conta.setValor(i == quantidade ? valorParcela.add(restoNaUltima) : valorParcela);
            conta.setVencimento(request.primeiroVencimento().plusMonths(i - 1L));
            conta.setGrupoParcelamentoId(grupoId);
            conta.setNumeroParcela(i);
            conta.setTotalParcelas(quantidade);
            parcelas.add(contaRepository.save(conta));
        }
        return parcelas;
    }

    /**
     * Edita a conta — permitido mesmo já paga (ex: corrigir um valor/descrição digitado
     * errado), caso em que o lançamento financeiro gerado por {@link #marcarComoPaga} é
     * atualizado junto, senão ficaria com dado antigo divergente da conta.
     */
    @Transactional
    public Conta atualizar(Long id, ContaRequest request) {
        Conta conta = buscarPorId(id);
        aplicarRequest(conta, request);
        conta = contaRepository.save(conta);
        sincronizarLancamento(conta);
        return conta;
    }

    @Transactional
    public Conta marcarComoPaga(Long id) {
        Conta conta = buscarPorId(id);
        if (conta.getStatus() == StatusConta.PAGO) {
            throw new IllegalStateException("Conta #%d já está paga".formatted(id));
        }
        conta.setStatus(StatusConta.PAGO);
        conta.setPagoEm(Instant.now());
        conta = contaRepository.save(conta);
        sincronizarLancamento(conta);
        return conta;
    }

    /** Remove também o lançamento financeiro gerado pelo pagamento, se a conta já
     * estava paga — senão a exclusão deixaria uma receita/despesa "fantasma" no
     * Financeiro, sem a conta que a originou. */
    @Transactional
    public void excluir(Long id) {
        Conta conta = buscarPorId(id);
        lancamentoFinanceiroRepository.findByOrigemAndOrigemId(OrigemLancamento.CONTA, id)
                .ifPresent(lancamentoFinanceiroRepository::delete);
        contaRepository.delete(conta);
    }

    private void aplicarRequest(Conta conta, ContaRequest request) {
        conta.setTipo(request.tipo());
        conta.setDescricao(request.descricao());
        conta.setValor(request.valor());
        conta.setVencimento(request.vencimento());
    }

    /**
     * Cria (na primeira vez que a conta é paga) ou atualiza (se editada depois de já
     * paga) o {@link LancamentoFinanceiro} correspondente — DESPESA pra conta a pagar,
     * RECEITA pra conta a receber. Não faz nada se a conta ainda não foi paga: criar ou
     * editar uma conta pendente é só um lembrete de vencimento, dinheiro nenhum saiu/
     * entrou de verdade ainda.
     */
    private void sincronizarLancamento(Conta conta) {
        if (conta.getStatus() != StatusConta.PAGO) {
            return;
        }
        LancamentoFinanceiro lancamento = lancamentoFinanceiroRepository
                .findByOrigemAndOrigemId(OrigemLancamento.CONTA, conta.getId())
                .orElseGet(LancamentoFinanceiro::new);
        lancamento.setTipo(conta.getTipo() == TipoConta.PAGAR ? TipoLancamento.DESPESA : TipoLancamento.RECEITA);
        lancamento.setCategoria(conta.getTipo() == TipoConta.PAGAR ? "Conta a pagar" : "Conta a receber");
        lancamento.setValor(conta.getValor());
        lancamento.setDescricao(conta.getDescricao());
        lancamento.setOrigem(OrigemLancamento.CONTA);
        lancamento.setOrigemId(conta.getId());
        lancamentoFinanceiroRepository.save(lancamento);
    }
}
