package br.com.artecriativa.api.financeiro;

import br.com.artecriativa.api.common.RecursoNaoEncontradoException;
import br.com.artecriativa.api.estoque.MateriaPrimaService;
import br.com.artecriativa.api.financeiro.dto.ContaParceladaRequest;
import br.com.artecriativa.api.financeiro.dto.ContaRequest;
import br.com.artecriativa.api.financeiro.dto.ContaResponse;
import br.com.artecriativa.api.financeiro.dto.ItemMateriaPrimaCompraRequest;
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
 * <p>
 * Uma conta a pagar também pode ser a própria compra de matéria-prima (ver
 * {@link ContaRequest#itensMateriaPrima}) — nesse caso a entrada de estoque já é
 * registrada na criação ({@link #criar}/{@link #criarParcelada}, via
 * {@link MateriaPrimaService#registrarEntradaVinculadaAConta}), sem lançar despesa
 * duplicada: a despesa nasce normalmente só quando a conta é paga.
 */
@Service
@RequiredArgsConstructor
public class ContaService {

    private final ContaRepository contaRepository;
    private final LancamentoFinanceiroRepository lancamentoFinanceiroRepository;
    private final MateriaPrimaService materiaPrimaService;

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

    /** Monta o {@link ContaResponse} completo, incluindo os itens de matéria-prima
     * vinculados (vazio pra qualquer conta comum) — usado pelo controller em todos os
     * endpoints, pra nunca esquecer de popular {@code itensMateriaPrima}. */
    @Transactional(readOnly = true)
    public ContaResponse paraResponse(Conta conta) {
        return ContaResponse.de(conta, materiaPrimaService.buscarItensDeConta(conta.getId(), conta.getGrupoParcelamentoId()));
    }

    @Transactional
    public Conta criar(ContaRequest request) {
        List<ItemMateriaPrimaCompraRequest> itens = itensOuVazio(request.itensMateriaPrima());
        if (!itens.isEmpty()) {
            validarItens(itens, request.tipo(), request.valor(), request.custosExtras());
        }

        Conta conta = new Conta();
        aplicarRequest(conta, request);
        conta = contaRepository.save(conta);

        for (ItemMateriaPrimaCompraRequest item : itens) {
            materiaPrimaService.registrarEntradaVinculadaAConta(
                    item.materiaPrimaId(), item.quantidade(), item.valor(), conta.getId(), null);
        }
        return conta;
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
        List<ItemMateriaPrimaCompraRequest> itens = itensOuVazio(request.itensMateriaPrima());
        if (!itens.isEmpty()) {
            validarItens(itens, request.tipo(), request.valorTotal(), request.custosExtras());
        }

        int quantidade = request.quantidadeParcelas();
        BigDecimal valorParcela = request.valorTotal().divide(BigDecimal.valueOf(quantidade), 2, RoundingMode.DOWN);
        BigDecimal restoNaUltima = request.valorTotal().subtract(valorParcela.multiply(BigDecimal.valueOf(quantidade)));

        // Custos extras seguem o mesmo rateio de valorParcela (resto na última) — só
        // pra guardar um registro coerente por parcela, a validação em si é sempre
        // contra o total (ver validarItens).
        BigDecimal custosExtras = request.custosExtras() == null ? BigDecimal.ZERO : request.custosExtras();
        BigDecimal custosExtrasParcela = custosExtras.divide(BigDecimal.valueOf(quantidade), 2, RoundingMode.DOWN);
        BigDecimal restoCustosExtrasNaUltima = custosExtras.subtract(custosExtrasParcela.multiply(BigDecimal.valueOf(quantidade)));

        UUID grupoId = UUID.randomUUID();
        List<Conta> parcelas = new ArrayList<>();
        for (int i = 1; i <= quantidade; i++) {
            Conta conta = new Conta();
            conta.setTipo(request.tipo());
            conta.setDescricao("%s (parcela %d/%d)".formatted(request.descricao(), i, quantidade));
            conta.setValor(i == quantidade ? valorParcela.add(restoNaUltima) : valorParcela);
            conta.setCustosExtras(i == quantidade ? custosExtrasParcela.add(restoCustosExtrasNaUltima) : custosExtrasParcela);
            conta.setVencimento(request.primeiroVencimento().plusMonths(i - 1L));
            conta.setGrupoParcelamentoId(grupoId);
            conta.setNumeroParcela(i);
            conta.setTotalParcelas(quantidade);
            parcelas.add(contaRepository.save(conta));
        }

        // Uma vez só pro grupo inteiro (não por parcela) — o vínculo é com o
        // parcelamento como um todo, não com uma parcela específica.
        for (ItemMateriaPrimaCompraRequest item : itens) {
            materiaPrimaService.registrarEntradaVinculadaAConta(
                    item.materiaPrimaId(), item.quantidade(), item.valor(), null, grupoId);
        }
        return parcelas;
    }

    /**
     * Edita a conta — permitido mesmo já paga (ex: corrigir um valor/descrição digitado
     * errado), caso em que o lançamento financeiro gerado por {@link #marcarComoPaga} é
     * atualizado junto, senão ficaria com dado antigo divergente da conta. Numa conta
     * vinculada a compra de matéria-prima, o valor não pode mudar (não dá pra saber qual
     * item ajustar) — descrição/vencimento continuam livres.
     */
    @Transactional
    public Conta atualizar(Long id, ContaRequest request) {
        Conta conta = buscarPorId(id);
        if (request.valor().compareTo(conta.getValor()) != 0
                && materiaPrimaService.existeCompraVinculada(conta.getId(), conta.getGrupoParcelamentoId())) {
            throw new IllegalStateException(
                    "Não é possível editar o valor de uma conta vinculada a compra de matéria-prima; "
                            + "exclua e crie novamente.");
        }
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
     * Financeiro, sem a conta que a originou. Se a conta tinha compra de matéria-prima
     * vinculada, estorna o estoque também: numa avulsa, sempre; numa parcela de um
     * grupo, só quando é a última parcela restante do grupo (o vínculo é com o grupo
     * inteiro, não importa em que ordem as parcelas são excluídas). */
    @Transactional
    public void excluir(Long id) {
        Conta conta = buscarPorId(id);
        lancamentoFinanceiroRepository.findByOrigemAndOrigemId(OrigemLancamento.CONTA, id)
                .ifPresent(lancamentoFinanceiroRepository::delete);

        if (conta.getGrupoParcelamentoId() != null) {
            if (contaRepository.countByGrupoParcelamentoId(conta.getGrupoParcelamentoId()) == 1) {
                materiaPrimaService.estornarComprasVinculadasAGrupo(conta.getGrupoParcelamentoId());
            }
        } else {
            materiaPrimaService.estornarComprasVinculadasAConta(id);
        }

        contaRepository.delete(conta);
    }

    private void aplicarRequest(Conta conta, ContaRequest request) {
        conta.setTipo(request.tipo());
        conta.setDescricao(request.descricao());
        conta.setValor(request.valor());
        conta.setVencimento(request.vencimento());
        conta.setCustosExtras(request.custosExtras() == null ? BigDecimal.ZERO : request.custosExtras());
    }

    private static List<ItemMateriaPrimaCompraRequest> itensOuVazio(List<ItemMateriaPrimaCompraRequest> itens) {
        return itens == null ? List.of() : itens;
    }

    /** {@code soma(itens.valor) + custosExtras} tem que bater exato com o valor da
     * conta (ou valor total, na parcelada) — mesma exigência de exatidão que já existe
     * no rateio das parcelas. */
    private static void validarItens(List<ItemMateriaPrimaCompraRequest> itens, TipoConta tipo, BigDecimal valorConta,
                                       BigDecimal custosExtras) {
        if (tipo != TipoConta.PAGAR) {
            throw new IllegalStateException(
                    "Itens de matéria-prima só podem ser vinculados a uma conta do tipo PAGAR.");
        }
        BigDecimal extras = custosExtras == null ? BigDecimal.ZERO : custosExtras;
        BigDecimal somaItens = itens.stream()
                .map(ItemMateriaPrimaCompraRequest::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal soma = somaItens.add(extras);
        if (soma.compareTo(valorConta) != 0) {
            throw new IllegalStateException(
                    ("Os itens de matéria-prima (%s) + custos extras (%s) somam %s, que não bate com o valor "
                            + "da conta (%s).")
                            .formatted(somaItens.toPlainString(), extras.toPlainString(),
                                    soma.toPlainString(), valorConta.toPlainString()));
        }
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
        lancamento.setCategoria(categoriaDoLancamento(conta));
        lancamento.setValor(conta.getValor());
        lancamento.setDescricao(conta.getDescricao());
        lancamento.setOrigem(OrigemLancamento.CONTA);
        lancamento.setOrigemId(conta.getId());
        lancamentoFinanceiroRepository.save(lancamento);
    }

    /** "Compra de matéria-prima" (mesma categoria usada quando a compra vem direto de
     * "Registrar movimentação") se a conta tem qualquer item de matéria-prima vinculado
     * — mesmo que misturado com {@code custosExtras} (frete/ferramentas). Categoria
     * genérica "Conta a pagar"/"Conta a receber" fica só pra conta sem nenhum vínculo
     * (aluguel, assinatura, etc. — fora do escopo de matéria-prima). */
    private String categoriaDoLancamento(Conta conta) {
        if (materiaPrimaService.existeCompraVinculada(conta.getId(), conta.getGrupoParcelamentoId())) {
            return "Compra de matéria-prima";
        }
        return conta.getTipo() == TipoConta.PAGAR ? "Conta a pagar" : "Conta a receber";
    }
}
