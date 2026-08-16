package br.com.artecriativa.api.financeiro.dto;

import br.com.artecriativa.api.financeiro.TipoLancamento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LancamentoFinanceiroRequest(
        @NotNull(message = "tipo é obrigatório") TipoLancamento tipo,
        @NotBlank(message = "categoria é obrigatória") String categoria,
        @NotNull(message = "valor é obrigatório")
        @DecimalMin(value = "0.0", inclusive = false, message = "valor deve ser maior que zero") BigDecimal valor,
        String descricao,
        @NotNull(message = "data do lançamento é obrigatória") LocalDate dataLancamento
) {
}
