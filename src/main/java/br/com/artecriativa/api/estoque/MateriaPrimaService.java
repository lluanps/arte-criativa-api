package br.com.artecriativa.api.estoque;

import br.com.artecriativa.api.common.FormatoNumerico;
import br.com.artecriativa.api.common.MensagemVinculo;
import br.com.artecriativa.api.common.RecursoNaoEncontradoException;
import br.com.artecriativa.api.estoque.dto.MateriaPrimaRequest;
import br.com.artecriativa.api.estoque.dto.MovimentacaoMateriaPrimaRequest;
import br.com.artecriativa.api.financeiro.LancamentoFinanceiro;
import br.com.artecriativa.api.financeiro.LancamentoFinanceiroRepository;
import br.com.artecriativa.api.financeiro.OrigemLancamento;
import br.com.artecriativa.api.financeiro.TipoLancamento;
import br.com.artecriativa.api.producao.ReceitaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MateriaPrimaService {

    private final MateriaPrimaRepository materiaPrimaRepository;
    private final MovimentacaoMateriaPrimaRepository movimentacaoRepository;
    private final ReceitaRepository receitaRepository;
    private final LancamentoFinanceiroRepository lancamentoFinanceiroRepository;

    public List<MateriaPrima> listarTodas() {
        return materiaPrimaRepository.findAll();
    }

    public MateriaPrima buscarPorId(Long id) {
        return materiaPrimaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Matéria-prima não encontrada: " + id));
    }

    @Transactional
    public MateriaPrima criar(MateriaPrimaRequest request) {
        MateriaPrima materiaPrima = new MateriaPrima();
        aplicarRequest(materiaPrima, request);
        return materiaPrimaRepository.save(materiaPrima);
    }

    @Transactional
    public MateriaPrima atualizar(Long id, MateriaPrimaRequest request) {
        MateriaPrima materiaPrima = buscarPorId(id);
        aplicarRequest(materiaPrima, request);
        return materiaPrimaRepository.save(materiaPrima);
    }

    @Transactional
    public void excluir(Long id) {
        MateriaPrima materiaPrima = buscarPorId(id);

        List<String> vinculos = new ArrayList<>();
        MensagemVinculo.add(vinculos, movimentacaoRepository.countByMateriaPrimaId(id),
                "movimentação de estoque", "movimentações de estoque");
        MensagemVinculo.add(vinculos, receitaRepository.countByItens_MateriaPrimaId(id),
                "ficha técnica", "fichas técnicas");
        if (!vinculos.isEmpty()) {
            throw new IllegalStateException(
                    "Não é possível excluir '%s': existem %s vinculados a esta matéria-prima."
                            .formatted(materiaPrima.getNome(), MensagemVinculo.juntarComE(vinculos)));
        }

        materiaPrimaRepository.delete(materiaPrima);
    }

    /**
     * Registra uma entrada (compra) ou saída (consumo/perda) de matéria-prima
     * e atualiza o saldo atual. Impede que o saldo fique negativo.
     */
    @Transactional
    public MovimentacaoMateriaPrima registrarMovimentacao(Long materiaPrimaId, MovimentacaoMateriaPrimaRequest request) {
        MateriaPrima materiaPrima = buscarPorId(materiaPrimaId);
        BigDecimal estoqueAntes = materiaPrima.getEstoqueAtual();

        BigDecimal novoEstoque = switch (request.tipo()) {
            case ENTRADA -> estoqueAntes.add(request.quantidade());
            case SAIDA -> estoqueAntes.subtract(request.quantidade());
        };

        if (novoEstoque.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException(
                    "Estoque insuficiente da matéria-prima '%s'. Disponível: %s, solicitado: %s"
                            .formatted(materiaPrima.getNome(),
                                    FormatoNumerico.semZerosDesnecessarios(materiaPrima.getEstoqueAtual()),
                                    FormatoNumerico.semZerosDesnecessarios(request.quantidade())));
        }

        BigDecimal custoUnitarioApurado = null;
        if (request.valorPago() != null) {
            if (request.tipo() != TipoMovimentacao.ENTRADA) {
                throw new IllegalStateException("Valor pago só se aplica a uma entrada (compra) de matéria-prima.");
            }
            custoUnitarioApurado = request.valorPago().divide(request.quantidade(), 4, RoundingMode.HALF_UP);
            materiaPrima.setCustoUnitario(
                    custoMedioPonderado(estoqueAntes, materiaPrima.getCustoUnitario(), request.quantidade(), custoUnitarioApurado));
        }

        MovimentacaoMateriaPrima movimentacao = new MovimentacaoMateriaPrima();
        movimentacao.setMateriaPrima(materiaPrima);
        movimentacao.setTipo(request.tipo());
        movimentacao.setMotivo(request.motivo());
        movimentacao.setQuantidade(request.quantidade());
        movimentacao.setValorPago(request.valorPago());
        movimentacao.setCustoUnitarioApurado(custoUnitarioApurado);
        movimentacao.setObservacao(request.observacao());

        materiaPrima.setEstoqueAtual(novoEstoque);
        materiaPrimaRepository.save(materiaPrima);
        movimentacao = movimentacaoRepository.save(movimentacao);

        // Só lança despesa quando teve valor pago de verdade (uma compra) — entrada de
        // AJUSTE/PRODUCAO sem valorPago não representa dinheiro saindo do caixa.
        if (request.valorPago() != null) {
            LancamentoFinanceiro lancamento = new LancamentoFinanceiro();
            lancamento.setTipo(TipoLancamento.DESPESA);
            lancamento.setCategoria("Compra de matéria-prima");
            lancamento.setValor(request.valorPago());
            lancamento.setDescricao("Compra de %s %s de %s".formatted(
                    FormatoNumerico.semZerosDesnecessarios(request.quantidade()),
                    materiaPrima.getUnidadeMedida(),
                    materiaPrima.getNome()));
            lancamento.setOrigem(OrigemLancamento.COMPRA);
            lancamento.setOrigemId(movimentacao.getId());
            lancamentoFinanceiroRepository.save(lancamento);
        }

        return movimentacao;
    }

    /**
     * Custo médio ponderado entre o que já tinha em estoque (a um custo) e o que acabou
     * de entrar (a outro custo) — evita que o custo da ficha técnica pule bruscamente a
     * cada compra feita a um preço diferente do anterior.
     */
    private static BigDecimal custoMedioPonderado(BigDecimal qtdAntiga, BigDecimal custoAntigo,
                                                    BigDecimal qtdNova, BigDecimal custoNovo) {
        BigDecimal qtdTotal = qtdAntiga.add(qtdNova);
        if (qtdTotal.compareTo(BigDecimal.ZERO) == 0) {
            return custoNovo;
        }
        BigDecimal valorTotal = qtdAntiga.multiply(custoAntigo).add(qtdNova.multiply(custoNovo));
        return valorTotal.divide(qtdTotal, 4, RoundingMode.HALF_UP);
    }

    public List<MovimentacaoMateriaPrima> listarMovimentacoes(Long materiaPrimaId) {
        buscarPorId(materiaPrimaId);
        return movimentacaoRepository.findByMateriaPrimaIdOrderByDataMovimentacaoDesc(materiaPrimaId);
    }

    private void aplicarRequest(MateriaPrima materiaPrima, MateriaPrimaRequest request) {
        materiaPrima.setNome(request.nome());
        materiaPrima.setUnidadeMedida(request.unidadeMedida());
        materiaPrima.setCustoUnitario(request.custoUnitario());
        materiaPrima.setEstoqueMinimo(request.estoqueMinimo());
        materiaPrima.setVolumeMl(request.volumeMl());
        materiaPrima.setFornecedor(request.fornecedor());
    }
}
