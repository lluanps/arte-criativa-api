package br.com.artecriativa.api.producao.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ReceitaItemRequest(
        @NotNull(message = "matéria-prima é obrigatória") Long materiaPrimaId,
        @NotNull(message = "quantidade é obrigatória")
        @DecimalMin(value = "0.0", inclusive = false, message = "quantidade deve ser maior que zero") BigDecimal quantidade,
        /** Opcional — quando omitida, usa a mesma unidade cadastrada na matéria-prima
         * (nenhuma conversão é feita). Só precisa preencher se quiser escrever a
         * quantidade numa unidade diferente (ex: "g" numa matéria-prima cadastrada em
         * "kg") — ver {@code UnidadeMedida.converter}. */
        String unidadeMedida
) {
}
