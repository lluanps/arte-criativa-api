package br.com.artecriativa.api.financeiro.dto;

import br.com.artecriativa.api.financeiro.TipoConta;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContaRequest(
        @NotNull(message = "tipo é obrigatório") TipoConta tipo,
        @NotBlank(message = "descrição é obrigatória") String descricao,
        @NotNull(message = "valor é obrigatório")
        @DecimalMin(value = "0.0", inclusive = false, message = "valor deve ser maior que zero") BigDecimal valor,
        @NotNull(message = "vencimento é obrigatório") LocalDate vencimento
) {
}
