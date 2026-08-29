package br.com.artecriativa.api.vendas.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReagendarEntregaRequest(
        @NotNull(message = "nova data de entrega é obrigatória")
        LocalDate novaDataEntrega
) {
}
