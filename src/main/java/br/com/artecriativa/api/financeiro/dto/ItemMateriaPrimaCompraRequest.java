package br.com.artecriativa.api.financeiro.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Um item de matéria-prima comprado junto com uma conta a pagar (ver javadoc de
 * {@code ContaRequest#itensMateriaPrima}). A soma de {@code valor} de todos os itens
 * tem que bater exato com o valor da conta (ou valor total, na parcelada).
 */
public record ItemMateriaPrimaCompraRequest(
        @NotNull(message = "matéria-prima é obrigatória") Long materiaPrimaId,
        @NotNull(message = "quantidade é obrigatória")
        @DecimalMin(value = "0.0", inclusive = false, message = "quantidade deve ser maior que zero") BigDecimal quantidade,
        @NotNull(message = "valor é obrigatório")
        @DecimalMin(value = "0.0", inclusive = false, message = "valor deve ser maior que zero") BigDecimal valor
) {
}
