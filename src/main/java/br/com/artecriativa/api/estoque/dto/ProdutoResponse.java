package br.com.artecriativa.api.estoque.dto;

import br.com.artecriativa.api.estoque.Produto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ProdutoResponse(
        Long id,
        String nome,
        String descricao,
        Long categoriaId,
        String categoriaNome,
        BigDecimal volumeMl,
        BigDecimal precoVenda,
        BigDecimal margemDesejadaPercentual,
        BigDecimal estoqueAtual,
        BigDecimal estoqueMinimo,
        List<String> fotosUrls,
        boolean ativo,
        Instant criadoEm,
        Instant atualizadoEm
) {
    public static ProdutoResponse de(Produto produto) {
        return new ProdutoResponse(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getCategoria() != null ? produto.getCategoria().getId() : null,
                produto.getCategoria() != null ? produto.getCategoria().getNome() : null,
                produto.getVolumeMl(),
                produto.getPrecoVenda(),
                produto.getMargemDesejadaPercentual(),
                produto.getEstoqueAtual(),
                produto.getEstoqueMinimo(),
                // List.copyOf força a leitura da coleção agora — em ProdutoRepository.buscar
                // (paginação) fotosUrls é LAZY de propósito, e só ler a referência sem
                // iterar (o que só um getFotosUrls() puro faz) não materializa: o proxy do
                // Hibernate só inicializa no primeiro acesso de verdade (ex: iterar), e se
                // isso acontecer só na hora do Jackson serializar (já fora da transação),
                // estoura "could not initialize proxy - no Session". Em findAll()/findById()
                // (fetch eager) isso é só uma cópia extra barata, sem efeito nenhum.
                List.copyOf(produto.getFotosUrls()),
                produto.isAtivo(),
                produto.getCriadoEm(),
                produto.getAtualizadoEm()
        );
    }
}
