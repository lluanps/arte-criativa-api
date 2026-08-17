package br.com.artecriativa.api.estoque.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record MateriaPrimaRequest(
        @NotBlank(message = "nome é obrigatório") String nome,
        @NotBlank(message = "unidade de medida é obrigatória") String unidadeMedida,
        @DecimalMin(value = "0.0", message = "custo unitário não pode ser negativo") BigDecimal custoUnitario,
        @DecimalMin(value = "0.0", message = "estoque mínimo não pode ser negativo") BigDecimal estoqueMinimo,
        @DecimalMin(value = "0.0", message = "volume não pode ser negativo") BigDecimal volumeMl,
        String fornecedor
) {
    public MateriaPrimaRequest {
        if (custoUnitario == null) {
            custoUnitario = BigDecimal.ZERO;
        }
        if (estoqueMinimo == null) {
            estoqueMinimo = BigDecimal.ZERO;
        }
    }
}
