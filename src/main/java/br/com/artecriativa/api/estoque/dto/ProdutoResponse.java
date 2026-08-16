package br.com.artecriativa.api.estoque.dto;

import br.com.artecriativa.api.estoque.Produto;

import java.math.BigDecimal;
import java.time.Instant;

public record ProdutoResponse(
        Long id,
        String nome,
        String descricao,
        String categoria,
        BigDecimal precoVenda,
        BigDecimal estoqueAtual,
        BigDecimal estoqueMinimo,
        String fotoUrl,
        boolean ativo,
        Instant criadoEm,
        Instant atualizadoEm
) {
    public static ProdutoResponse de(Produto produto) {
        return new ProdutoResponse(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getCategoria(),
                produto.getPrecoVenda(),
                produto.getEstoqueAtual(),
                produto.getEstoqueMinimo(),
                produto.getFotoUrl(),
                produto.isAtivo(),
                produto.getCriadoEm(),
                produto.getAtualizadoEm()
        );
    }
}
