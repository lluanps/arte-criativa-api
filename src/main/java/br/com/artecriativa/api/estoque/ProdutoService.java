package br.com.artecriativa.api.estoque;

import br.com.artecriativa.api.cadastros.Categoria;
import br.com.artecriativa.api.cadastros.CategoriaRepository;
import br.com.artecriativa.api.common.FormatoNumerico;
import br.com.artecriativa.api.common.RecursoNaoEncontradoException;
import br.com.artecriativa.api.estoque.dto.MovimentacaoProdutoRequest;
import br.com.artecriativa.api.estoque.dto.ProdutoRequest;
import br.com.artecriativa.api.producao.ProducaoRepository;
import br.com.artecriativa.api.producao.ReceitaRepository;
import br.com.artecriativa.api.tutoriais.TutorialRepository;
import br.com.artecriativa.api.vendas.VendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final MovimentacaoProdutoRepository movimentacaoRepository;
    private final CategoriaRepository categoriaRepository;
    private final VendaRepository vendaRepository;
    private final ProducaoRepository producaoRepository;
    private final ReceitaRepository receitaRepository;
    private final TutorialRepository tutorialRepository;

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

    /**
     * Checa proativamente (em vez de deixar o banco recusar por FK) e recusa com uma
     * mensagem que diz exatamente o que está vinculado, pra a pessoa saber se é seguro
     * excluir de vez ou se precisa desativar.
     */
    @Transactional
    public void excluir(Long id) {
        Produto produto = buscarPorId(id);

        List<String> vinculos = descreverVinculos(id);
        if (!vinculos.isEmpty()) {
            throw new IllegalStateException(
                    "Não é possível excluir '%s': existem %s vinculados a este item."
                            .formatted(produto.getNome(), juntarComE(vinculos)));
        }

        produtoRepository.delete(produto);
    }

    /**
     * Exclui o produto "de vez", levando junto movimentações de estoque, produções e a
     * ficha técnica (receita), e desvinculando (sem apagar) tutoriais relacionados —
     * pensado pra corrigir um cadastro feito por engano.
     * <p>
     * Só permite quando o produto nunca teve venda de verdade: excluir nesse caso
     * apagaria histórico de faturamento (venda + lançamento financeiro), o que não dá
     * pra desfazer. Quando há venda, o chamador deve oferecer desativar em vez disso.
     */
    @Transactional
    public void excluirDefinitivamente(Long id) {
        Produto produto = buscarPorId(id);

        if (vendaRepository.countByItens_ProdutoId(id) > 0) {
            throw new IllegalStateException(
                    "Não é possível excluir definitivamente: '%s' já teve venda registrada — excluir apagaria o histórico de faturamento. Desative o produto em vez disso."
                            .formatted(produto.getNome()));
        }

        movimentacaoRepository.deleteByProdutoId(id);
        producaoRepository.deleteByProdutoId(id);
        receitaRepository.findByProdutoId(id).ifPresent(receitaRepository::delete);
        tutorialRepository.findByProdutoRelacionadoId(id).forEach(tutorial -> {
            tutorial.setProdutoRelacionado(null);
            tutorialRepository.save(tutorial);
        });

        produtoRepository.delete(produto);
    }

    /**
     * Lista, em português, o que está impedindo a exclusão simples do produto — cada
     * item já vem no plural/singular certo e com a contagem, ex: "3 movimentações de
     * estoque", "1 venda", "ficha técnica".
     */
    private List<String> descreverVinculos(Long produtoId) {
        List<String> vinculos = new ArrayList<>();

        long movimentacoes = movimentacaoRepository.countByProdutoId(produtoId);
        if (movimentacoes > 0) {
            vinculos.add(movimentacoes == 1 ? "1 movimentação de estoque" : movimentacoes + " movimentações de estoque");
        }

        long vendas = vendaRepository.countByItens_ProdutoId(produtoId);
        if (vendas > 0) {
            vinculos.add(vendas == 1 ? "1 venda" : vendas + " vendas");
        }

        long producoes = producaoRepository.countByProdutoId(produtoId);
        if (producoes > 0) {
            vinculos.add(producoes == 1 ? "1 produção registrada" : producoes + " produções registradas");
        }

        if (receitaRepository.findByProdutoId(produtoId).isPresent()) {
            vinculos.add("ficha técnica");
        }

        long tutoriais = tutorialRepository.countByProdutoRelacionadoId(produtoId);
        if (tutoriais > 0) {
            vinculos.add(tutoriais == 1 ? "1 tutorial" : tutoriais + " tutoriais");
        }

        return vinculos;
    }

    private String juntarComE(List<String> itens) {
        if (itens.size() == 1) {
            return itens.get(0);
        }
        return String.join(", ", itens.subList(0, itens.size() - 1)) + " e " + itens.get(itens.size() - 1);
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
                            .formatted(produto.getNome(),
                                    FormatoNumerico.semZerosDesnecessarios(produto.getEstoqueAtual()),
                                    FormatoNumerico.semZerosDesnecessarios(request.quantidade())));
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
        produto.setMargemDesejadaPercentual(request.margemDesejadaPercentual());
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
