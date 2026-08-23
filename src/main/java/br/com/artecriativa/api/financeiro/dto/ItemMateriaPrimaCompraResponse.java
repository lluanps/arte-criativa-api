package br.com.artecriativa.api.financeiro.dto;

import java.math.BigDecimal;

public record ItemMateriaPrimaCompraResponse(
        Long materiaPrimaId,
        String materiaPrimaNome,
        BigDecimal quantidade,
        BigDecimal valor
) {
}
