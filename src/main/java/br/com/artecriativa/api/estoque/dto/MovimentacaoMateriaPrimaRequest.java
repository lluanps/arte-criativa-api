package br.com.artecriativa.api.estoque.dto;

import br.com.artecriativa.api.estoque.MotivoMovimentacaoMateriaPrima;
import br.com.artecriativa.api.estoque.TipoMovimentacao;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MovimentacaoMateriaPrimaRequest(
        @NotNull(message = "tipo é obrigatório") TipoMovimentacao tipo,
        @NotNull(message = "motivo é obrigatório") MotivoMovimentacaoMateriaPrima motivo,
        @NotNull(message = "quantidade é obrigatória")
        @DecimalMin(value = "0.0", inclusive = false, message = "quantidade deve ser maior que zero") BigDecimal quantidade,
        String observacao
) {
}
