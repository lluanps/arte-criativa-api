package br.com.artecriativa.api.vendas.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record VendaRequest(
        Long clienteId,
        Long canalId,
        @NotEmpty(message = "venda precisa de ao menos um item")
        List<@Valid VendaItemRequest> itens,
        /** Preenchido = encomenda (nasce PENDENTE); null = venda de balcão (comportamento
         * atual, nasce ENTREGUE). */
        LocalDate dataEntregaPrevista,
        /** Só aplicável junto de {@code dataEntregaPrevista}. Null/omitido = sem sinal. */
        @DecimalMin(value = "0.0", message = "sinal não pode ser negativo")
        BigDecimal valorSinal
) {
}
