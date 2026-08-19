package br.com.artecriativa.api.estoque;

import br.com.artecriativa.api.cadastros.Categoria;
import br.com.artecriativa.api.cadastros.CategoriaRepository;
import br.com.artecriativa.api.common.FormatoNumerico;
import br.com.artecriativa.api.common.MensagemVinculo;
import br.com.artecriativa.api.common.RecursoNaoEncontradoException;
import br.com.artecriativa.api.estoque.dto.MovimentacaoProdutoRequest;
import br.com.artecriativa.api.estoque.dto.ProdutoRequest;
import br.com.artecriativa.api.ideias.IdeiaRepository;
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
    private final IdeiaRepository ideiaRepository;

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
                            .formatted(produto.getNome(), MensagemVinculo.juntarComE(vinculos)));
        }

        desvincularIdeias(id);
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
        desvincularIdeias(id);

        produtoRepository.delete(produto);
    }

    /** Ideia vinculada a um produto é só uma referência leve — nunca bloqueia a exclusão. */
    private void desvincularIdeias(Long produtoId) {
        ideiaRepository.findByProdutoRelacionadoId(produtoId).forEach(ideia -> {
            ideia.setProdutoRelacionado(null);
            ideiaRepository.save(ideia);
        });
    }

    /**
     * Lista, em português, o que está impedindo a exclusão simples do produto — cada
     * item já vem no plural/singular certo e com a contagem, ex: "3 movimentações de
     * estoque", "1 venda", "ficha técnica".
     */
    private List<String> descreverVinculos(Long produtoId) {
        List<String> vinculos = new ArrayList<>();
        MensagemVinculo.add(vinculos, movimentacaoRepository.countByProdutoId(produtoId),
                "movimentação de estoque", "movimentações de estoque");
        MensagemVinculo.add(vinculos, vendaRepository.countByItens_ProdutoId(produtoId), "venda", "vendas");
        MensagemVinculo.add(vinculos, producaoRepository.countByProdutoId(produtoId),
                "produção registrada", "produções registradas");
        if (receitaRepository.findByProdutoId(produtoId).isPresent()) {
            vinculos.add("ficha técnica");
        }
        MensagemVinculo.add(vinculos, tutorialRepository.countByProdutoRelacionadoId(produtoId), "tutorial", "tutoriais");
        return vinculos;
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
