package br.com.artecriativa.api.estoque.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProdutoRequest(
        @NotBlank(message = "nome é obrigatório") String nome,
        String descricao,
        String categoria,
        @NotNull(message = "preço de venda é obrigatório")
        @DecimalMin(value = "0.0", message = "preço de venda não pode ser negativo") BigDecimal precoVenda,
        @DecimalMin(value = "0.0", message = "estoque mínimo não pode ser negativo") BigDecimal estoqueMinimo,
        String fotoUrl,
        Boolean ativo
) {
    public ProdutoRequest {
        if (estoqueMinimo == null) {
            estoqueMinimo = BigDecimal.ZERO;
        }
    }
}
