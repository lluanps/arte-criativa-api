package br.com.artecriativa.api.estoque;

import br.com.artecriativa.api.common.FormatoNumerico;
import br.com.artecriativa.api.common.MensagemVinculo;
import br.com.artecriativa.api.common.PaginaResponse;
import br.com.artecriativa.api.common.RecursoNaoEncontradoException;
import br.com.artecriativa.api.estoque.dto.MateriaPrimaAtualizacaoRequest;
import br.com.artecriativa.api.estoque.dto.MateriaPrimaRequest;
import br.com.artecriativa.api.estoque.dto.MateriaPrimaResponse;
import br.com.artecriativa.api.estoque.dto.MovimentacaoMateriaPrimaRequest;
import br.com.artecriativa.api.financeiro.LancamentoFinanceiro;
import br.com.artecriativa.api.financeiro.LancamentoFinanceiroRepository;
import br.com.artecriativa.api.financeiro.OrigemLancamento;
import br.com.artecriativa.api.financeiro.TipoLancamento;
import br.com.artecriativa.api.producao.ReceitaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MateriaPrimaService {

    private final MateriaPrimaRepository materiaPrimaRepository;
    private final MovimentacaoMateriaPrimaRepository movimentacaoRepository;
    private final ReceitaRepository receitaRepository;
    private final LancamentoFinanceiroRepository lancamentoFinanceiroRepository;

    private static final Map<String, String> CAMPOS_ORDENACAO = Map.of(
            "nome", "nome",
            "unidadeMedida", "unidadeMedida",
            "custoUnitario", "custoUnitario",
            "estoqueAtual", "estoqueAtual");

    public List<MateriaPrima> listarTodas() {
        return materiaPrimaRepository.findAll();
    }

    /** Busca paginada com filtros pra tela de listagem (ver {@link MateriaPrimaRepository#buscar}). */
    public PaginaResponse<MateriaPrimaResponse> buscarPaginado(String busca, boolean estoqueBaixo, int pagina,
                                                                 int tamanho, String ordenarPor, String direcao) {
        String buscaNormalizada = (busca == null || busca.isBlank()) ? "" : busca.trim();
        String campo = CAMPOS_ORDENACAO.getOrDefault(ordenarPor, "nome");
        Sort.Direction dir = "desc".equalsIgnoreCase(direcao) ? Sort.Direction.DESC : Sort.Direction.ASC;
        int tamanhoValido = Math.min(Math.max(tamanho, 1), 100);
        Pageable pageable = PageRequest.of(Math.max(pagina, 0), tamanhoValido, Sort.by(dir, campo));

        return PaginaResponse.de(
                materiaPrimaRepository.buscar(buscaNormalizada, estoqueBaixo, pageable),
                MateriaPrimaResponse::de);
    }

    public MateriaPrima buscarPorId(Long id) {
        return materiaPrimaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Matéria-prima não encontrada: " + id));
    }

    /**
     * Criar é sempre "registrar a primeira compra" (ver javadoc de
     * {@link MateriaPrimaRequest}) — nasce com custo unitário calculado
     * (valorPago ÷ quantidadeComprada), estoque já preenchido, uma
     * {@link MovimentacaoMateriaPrima} de COMPRA registrada pro histórico, e a
     * despesa correspondente lançada no Financeiro. Mesma lógica de
     * {@link #registrarMovimentacao}, só que aqui a matéria-prima ainda nem existe.
     */
    @Transactional
    public MateriaPrima criar(MateriaPrimaRequest request) {
        MateriaPrima materiaPrima = new MateriaPrima();
        aplicarMetadados(materiaPrima, request.nome(), request.unidadeMedida(),
                request.estoqueMinimo(), request.volumeMl(), request.fornecedor());

        BigDecimal custoUnitario = request.valorPago().divide(request.quantidadeComprada(), 4, RoundingMode.HALF_UP);
        materiaPrima.setCustoUnitario(custoUnitario);
        materiaPrima.setEstoqueAtual(request.quantidadeComprada());
        materiaPrima = materiaPrimaRepository.save(materiaPrima);

        MovimentacaoMateriaPrima movimentacao = new MovimentacaoMateriaPrima();
        movimentacao.setMateriaPrima(materiaPrima);
        movimentacao.setTipo(TipoMovimentacao.ENTRADA);
        movimentacao.setMotivo(MotivoMovimentacaoMateriaPrima.COMPRA);
        movimentacao.setQuantidade(request.quantidadeComprada());
        movimentacao.setValorPago(request.valorPago());
        movimentacao.setCustoUnitarioApurado(custoUnitario);
        movimentacao.setObservacao("Compra inicial (cadastro da matéria-prima)");
        movimentacao = movimentacaoRepository.save(movimentacao);

        lancarDespesaCompra(materiaPrima, movimentacao, request.quantidadeComprada(), request.valorPago());

        return materiaPrima;
    }

    /** Só metadados — custo unitário e estoque não entram aqui de propósito, ver
     * javadoc de {@link MateriaPrimaAtualizacaoRequest}. */
    @Transactional
    public MateriaPrima atualizar(Long id, MateriaPrimaAtualizacaoRequest request) {
        MateriaPrima materiaPrima = buscarPorId(id);
        aplicarMetadados(materiaPrima, request.nome(), request.unidadeMedida(),
                request.estoqueMinimo(), request.volumeMl(), request.fornecedor());
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
            lancarDespesaCompra(materiaPrima, movimentacao, request.quantidade(), request.valorPago());
        }

        return movimentacao;
    }

    /** Compartilhado por {@link #criar} (primeira compra) e {@link #registrarMovimentacao}
     * (compras seguintes) — mesmo formato de despesa nos dois casos. */
    private void lancarDespesaCompra(MateriaPrima materiaPrima, MovimentacaoMateriaPrima movimentacao,
                                       BigDecimal quantidade, BigDecimal valorPago) {
        LancamentoFinanceiro lancamento = new LancamentoFinanceiro();
        lancamento.setTipo(TipoLancamento.DESPESA);
        lancamento.setCategoria("Compra de matéria-prima");
        lancamento.setValor(valorPago);
        lancamento.setDescricao("Compra de %s %s de %s".formatted(
                FormatoNumerico.semZerosDesnecessarios(quantidade),
                materiaPrima.getUnidadeMedida(),
                materiaPrima.getNome()));
        lancamento.setOrigem(OrigemLancamento.COMPRA);
        lancamento.setOrigemId(movimentacao.getId());
        lancamentoFinanceiroRepository.save(lancamento);
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

    private void aplicarMetadados(MateriaPrima materiaPrima, String nome, String unidadeMedida,
                                    BigDecimal estoqueMinimo, BigDecimal volumeMl, String fornecedor) {
        materiaPrima.setNome(nome);
        materiaPrima.setUnidadeMedida(unidadeMedida);
        materiaPrima.setEstoqueMinimo(estoqueMinimo);
        materiaPrima.setVolumeMl(volumeMl);
        materiaPrima.setFornecedor(fornecedor);
    }
}
