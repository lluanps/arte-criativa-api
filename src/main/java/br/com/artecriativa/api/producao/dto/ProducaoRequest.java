package br.com.artecriativa.api.producao.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProducaoRequest(
        @NotNull(message = "produto é obrigatório") Long produtoId,
        @NotNull(message = "quantidade produzida é obrigatória")
        @DecimalMin(value = "0.0", inclusive = false, message = "quantidade produzida deve ser maior que zero") BigDecimal quantidadeProduzida,
        String observacao
) {
}
