package br.com.artecriativa.api.estoque;

import br.com.artecriativa.api.cadastros.Categoria;
import br.com.artecriativa.api.cadastros.CategoriaRepository;
import br.com.artecriativa.api.common.RecursoNaoEncontradoException;
import br.com.artecriativa.api.estoque.dto.MovimentacaoProdutoRequest;
import br.com.artecriativa.api.estoque.dto.ProdutoRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final MovimentacaoProdutoRepository movimentacaoRepository;
    private final CategoriaRepository categoriaRepository;

    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    public Produto buscarPorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado: " + id));
    }

    @Transactional
    public Produto criar(ProdutoRequest request) {
        Produto produto = new Produto();
        aplicarRequest(produto, request);
        return produtoRepository.save(produto);
    }

    @Transactional
    public Produto atualizar(Long id, ProdutoRequest request) {
        Produto produto = buscarPorId(id);
        aplicarRequest(produto, request);
        return produtoRepository.save(produto);
    }

    @Transactional
    public void excluir(Long id) {
        Produto produto = buscarPorId(id);
        produtoRepository.delete(produto);
    }

    /**
     * Registra uma entrada ou saída de estoque para o produto e atualiza o saldo atual.
     * Impede que o saldo fique negativo.
     */
    @Transactional
    public MovimentacaoProduto registrarMovimentacao(Long produtoId, MovimentacaoProdutoRequest request) {
        Produto produto = buscarPorId(produtoId);

        BigDecimal novoEstoque = switch (request.tipo()) {
            case ENTRADA -> produto.getEstoqueAtual().add(request.quantidade());
            case SAIDA -> produto.getEstoqueAtual().subtract(request.quantidade());
        };

        if (novoEstoque.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException(
                    "Estoque insuficiente para o produto '%s'. Disponível: %s, solicitado: %s"
                            .formatted(produto.getNome(), produto.getEstoqueAtual(), request.quantidade()));
        }

        MovimentacaoProduto movimentacao = new MovimentacaoProduto();
        movimentacao.setProduto(produto);
        movimentacao.setTipo(request.tipo());
        movimentacao.setMotivo(request.motivo());
        movimentacao.setQuantidade(request.quantidade());
        movimentacao.setObservacao(request.observacao());

        produto.setEstoqueAtual(novoEstoque);
        produtoRepository.save(produto);

        return movimentacaoRepository.save(movimentacao);
    }

    public List<MovimentacaoProduto> listarMovimentacoes(Long produtoId) {
        buscarPorId(produtoId);
        return movimentacaoRepository.findByProdutoIdOrderByDataMovimentacaoDesc(produtoId);
    }

    private void aplicarRequest(Produto produto, ProdutoRequest request) {
        produto.setNome(request.nome());
        produto.setDescricao(request.descricao());
        produto.setCategoria(buscarCategoria(request.categoriaId()));
        produto.setVolumeMl(request.volumeMl());
        produto.setPrecoVenda(request.precoVenda());
        produto.setEstoqueMinimo(request.estoqueMinimo());
        produto.setFotoUrl(request.fotoUrl());
        produto.setAtivo(request.ativo() == null || request.ativo());
    }

    private Categoria buscarCategoria(Long categoriaId) {
        if (categoriaId == null) {
            return null;
        }
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada: " + categoriaId));
    }
}
