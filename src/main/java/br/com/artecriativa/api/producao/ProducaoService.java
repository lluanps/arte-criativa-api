package br.com.artecriativa.api.producao;

import br.com.artecriativa.api.common.RecursoNaoEncontradoException;
import br.com.artecriativa.api.estoque.MateriaPrima;
import br.com.artecriativa.api.estoque.MateriaPrimaRepository;
import br.com.artecriativa.api.estoque.MotivoMovimentacaoMateriaPrima;
import br.com.artecriativa.api.estoque.MotivoMovimentacaoProduto;
import br.com.artecriativa.api.estoque.MovimentacaoMateriaPrima;
import br.com.artecriativa.api.estoque.MovimentacaoMateriaPrimaRepository;
import br.com.artecriativa.api.estoque.MovimentacaoProduto;
import br.com.artecriativa.api.estoque.MovimentacaoProdutoRepository;
import br.com.artecriativa.api.estoque.Produto;
import br.com.artecriativa.api.estoque.ProdutoRepository;
import br.com.artecriativa.api.estoque.TipoMovimentacao;
import br.com.artecriativa.api.estoque.UnidadeMedida;
import br.com.artecriativa.api.producao.dto.ProducaoRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Registra uma produção: a partir da {@link Receita} do produto, calcula o quanto de
 * cada matéria-prima é consumido (proporcional ao rendimento da receita), dá baixa no
 * estoque dessas matérias-primas, entrada no estoque do produto e calcula o custo total
 * com base no custo unitário de cada matéria-prima consumida + mão de obra/embalagem da
 * receita (por unidade produzida, opcionais — ver {@link Receita#getCustoMaoDeObra}).
 */
@Service
@RequiredArgsConstructor
public class ProducaoService {

    private static final int ESCALA_QUANTIDADE = 3;

    private final ProducaoRepository producaoRepository;
    private final ReceitaRepository receitaRepository;
    private final ProdutoRepository produtoRepository;
    private final MateriaPrimaRepository materiaPrimaRepository;
    private final MovimentacaoMateriaPrimaRepository movimentacaoMateriaPrimaRepository;
    private final MovimentacaoProdutoRepository movimentacaoProdutoRepository;

    public List<Producao> listarTodas() {
        return producaoRepository.findAll();
    }

    public Producao buscarPorId(Long id) {
        return producaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produção não encontrada: " + id));
    }

    public List<Producao> listarPorProduto(Long produtoId) {
        return producaoRepository.findByProdutoIdOrderByDataProducaoDesc(produtoId);
    }

    @Transactional
    public Producao registrar(ProducaoRequest request) {
        Produto produto = produtoRepository.findById(request.produtoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado: " + request.produtoId()));

        Receita receita = receitaRepository.findByProdutoId(produto.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Produto '%s' não possui receita cadastrada".formatted(produto.getNome())));

        BigDecimal fator = request.quantidadeProduzida()
                .divide(receita.getRendimento(), 6, RoundingMode.HALF_UP);

        BigDecimal custoTotal = BigDecimal.ZERO;
        for (ReceitaItem item : receita.getItens()) {
            MateriaPrima materiaPrima = item.getMateriaPrima();
            BigDecimal quantidadeNaUnidadeDaMateriaPrima = UnidadeMedida.converter(
                    item.getQuantidade(), item.getUnidadeMedida(), materiaPrima.getUnidadeMedida());
            BigDecimal consumo = quantidadeNaUnidadeDaMateriaPrima.multiply(fator).setScale(ESCALA_QUANTIDADE, RoundingMode.HALF_UP);

            BigDecimal novoEstoque = materiaPrima.getEstoqueAtual().subtract(consumo);
            if (novoEstoque.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalStateException(
                        "Estoque insuficiente da matéria-prima '%s'. Disponível: %s, necessário: %s"
                                .formatted(materiaPrima.getNome(), materiaPrima.getEstoqueAtual(), consumo));
            }

            custoTotal = custoTotal.add(consumo.multiply(materiaPrima.getCustoUnitario()));

            materiaPrima.setEstoqueAtual(novoEstoque);
            materiaPrimaRepository.save(materiaPrima);

            MovimentacaoMateriaPrima movimentacao = new MovimentacaoMateriaPrima();
            movimentacao.setMateriaPrima(materiaPrima);
            movimentacao.setTipo(TipoMovimentacao.SAIDA);
            movimentacao.setMotivo(MotivoMovimentacaoMateriaPrima.PRODUCAO);
            movimentacao.setQuantidade(consumo);
            movimentacao.setObservacao("Produção de %s %s".formatted(request.quantidadeProduzida(), produto.getNome()));
            movimentacaoMateriaPrimaRepository.save(movimentacao);
        }

        // Mão de obra e embalagem/outros são "por unidade produzida" (não por lote/rendimento,
        // ver Receita) — diferente do custo de insumo acima, que é por rendimento da receita.
        // Ambos são opcionais e já vêm 0 por padrão quando a ficha técnica não os preenche.
        BigDecimal custoIndiretoUnitario = receita.getCustoMaoDeObra().add(receita.getCustoEmbalagemOutros());
        custoTotal = custoTotal.add(custoIndiretoUnitario.multiply(request.quantidadeProduzida()));
        custoTotal = custoTotal.setScale(2, RoundingMode.HALF_UP);

        produto.setEstoqueAtual(produto.getEstoqueAtual().add(request.quantidadeProduzida()));
        produtoRepository.save(produto);

        MovimentacaoProduto movimentacaoProduto = new MovimentacaoProduto();
        movimentacaoProduto.setProduto(produto);
        movimentacaoProduto.setTipo(TipoMovimentacao.ENTRADA);
        movimentacaoProduto.setMotivo(MotivoMovimentacaoProduto.PRODUCAO);
        movimentacaoProduto.setQuantidade(request.quantidadeProduzida());
        movimentacaoProduto.setObservacao(request.observacao());
        movimentacaoProdutoRepository.save(movimentacaoProduto);

        Producao producao = new Producao();
        producao.setProduto(produto);
        producao.setQuantidadeProduzida(request.quantidadeProduzida());
        producao.setCustoTotal(custoTotal);
        producao.setObservacao(request.observacao());
        return producaoRepository.save(producao);
    }
}
