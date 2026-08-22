package br.com.artecriativa.api.producao;

import br.com.artecriativa.api.common.RecursoNaoEncontradoException;
import br.com.artecriativa.api.estoque.MateriaPrima;
import br.com.artecriativa.api.estoque.MateriaPrimaRepository;
import br.com.artecriativa.api.estoque.Produto;
import br.com.artecriativa.api.estoque.ProdutoRepository;
import br.com.artecriativa.api.estoque.UnidadeMedida;
import br.com.artecriativa.api.producao.dto.ReceitaItemRequest;
import br.com.artecriativa.api.producao.dto.ReceitaRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReceitaService {

    private final ReceitaRepository receitaRepository;
    private final ProdutoRepository produtoRepository;
    private final MateriaPrimaRepository materiaPrimaRepository;

    @Transactional(readOnly = true)
    public List<Receita> listarTodas() {
        return receitaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Receita buscarPorId(Long id) {
        return receitaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Receita não encontrada: " + id));
    }

    @Transactional(readOnly = true)
    public Receita buscarPorProdutoId(Long produtoId) {
        return receitaRepository.findByProdutoId(produtoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Produto não possui receita cadastrada: " + produtoId));
    }

    @Transactional
    public Receita criar(ReceitaRequest request) {
        Produto produto = buscarProduto(request.produtoId());
        receitaRepository.findByProdutoId(produto.getId()).ifPresent(existente -> {
            throw new IllegalStateException(
                    "Produto '%s' já possui uma receita cadastrada".formatted(produto.getNome()));
        });

        Receita receita = new Receita();
        receita.setProduto(produto);
        aplicarRequest(receita, request);
        return receitaRepository.save(receita);
    }

    @Transactional
    public Receita atualizar(Long id, ReceitaRequest request) {
        Receita receita = buscarPorId(id);

        if (!receita.getProduto().getId().equals(request.produtoId())) {
            Produto novoProduto = buscarProduto(request.produtoId());
            receitaRepository.findByProdutoId(novoProduto.getId()).ifPresent(existente -> {
                throw new IllegalStateException(
                        "Produto '%s' já possui uma receita cadastrada".formatted(novoProduto.getNome()));
            });
            receita.setProduto(novoProduto);
        }

        aplicarRequest(receita, request);
        return receitaRepository.save(receita);
    }

    @Transactional
    public void excluir(Long id) {
        Receita receita = buscarPorId(id);
        receitaRepository.delete(receita);
    }

    private void aplicarRequest(Receita receita, ReceitaRequest request) {
        receita.setNome(request.nome());
        receita.setRendimento(request.rendimento());
        receita.setCustoMaoDeObra(request.custoMaoDeObra());
        receita.setCustoEmbalagemOutros(request.custoEmbalagemOutros());

        List<ReceitaItem> itens = request.itens().stream().map(this::criarItem).toList();
        receita.substituirItens(itens);
    }

    private ReceitaItem criarItem(ReceitaItemRequest itemRequest) {
        MateriaPrima materiaPrima = materiaPrimaRepository.findById(itemRequest.materiaPrimaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Matéria-prima não encontrada: " + itemRequest.materiaPrimaId()));

        String unidade = itemRequest.unidadeMedida() != null && !itemRequest.unidadeMedida().isBlank()
                ? itemRequest.unidadeMedida()
                : materiaPrima.getUnidadeMedida();

        // Valida a conversão já na hora de salvar (não só quando alguém for ler a ficha
        // técnica depois) — se a unidade não for reconhecida ou for de outra grandeza,
        // o erro aparece aqui, com o item errado ainda visível no formulário.
        UnidadeMedida.converter(itemRequest.quantidade(), unidade, materiaPrima.getUnidadeMedida());

        ReceitaItem item = new ReceitaItem();
        item.setMateriaPrima(materiaPrima);
        item.setQuantidade(itemRequest.quantidade());
        item.setUnidadeMedida(unidade);
        return item;
    }

    private Produto buscarProduto(Long produtoId) {
        return produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado: " + produtoId));
    }
}
