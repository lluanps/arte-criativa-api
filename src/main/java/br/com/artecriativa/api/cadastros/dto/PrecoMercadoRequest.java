package br.com.artecriativa.api.cadastros.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PrecoMercadoRequest(
        @NotNull(message = "preço mínimo é obrigatório")
        @DecimalMin(value = "0.0", message = "preço mínimo não pode ser negativo") BigDecimal min,
        @NotNull(message = "preço máximo é obrigatório")
        @DecimalMin(value = "0.0", message = "preço máximo não pode ser negativo") BigDecimal max
) {
}
