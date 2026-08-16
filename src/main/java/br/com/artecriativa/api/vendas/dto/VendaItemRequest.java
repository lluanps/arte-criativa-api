package br.com.artecriativa.api.vendas.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record VendaItemRequest(
        @NotNull(message = "produto é obrigatório") Long produtoId,
        @NotNull(message = "quantidade é obrigatória")
        @DecimalMin(value = "0.0", inclusive = false, message = "quantidade deve ser maior que zero") BigDecimal quantidade,
        @DecimalMin(value = "0.0", inclusive = false, message = "preço unitário deve ser maior que zero")
        BigDecimal precoUnitario
) {
}
