package br.com.artecriativa.api.estoque.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record ProdutoRequest(
        @NotBlank(message = "nome é obrigatório") String nome,
        String descricao,
        Long categoriaId,
        @DecimalMin(value = "0.0", message = "volume não pode ser negativo") BigDecimal volumeMl,
        @NotNull(message = "preço de venda é obrigatório")
        @DecimalMin(value = "0.0", message = "preço de venda não pode ser negativo") BigDecimal precoVenda,
        @DecimalMin(value = "0.0", message = "margem desejada não pode ser negativa") BigDecimal margemDesejadaPercentual,
        @DecimalMin(value = "0.0", message = "estoque mínimo não pode ser negativo") BigDecimal estoqueMinimo,
        @Size(max = 5, message = "no máximo 5 fotos") List<String> fotosUrls,
        Boolean ativo
) {
    public ProdutoRequest {
        if (estoqueMinimo == null) {
            estoqueMinimo = BigDecimal.ZERO;
        }
        if (fotosUrls == null) {
            fotosUrls = List.of();
        }
    }
}
