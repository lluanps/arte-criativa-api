package br.com.artecriativa.api.vendas.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record VendaRequest(
        Long clienteId,
        Long canalId,
        @NotEmpty(message = "venda precisa de ao menos um item")
        List<@Valid VendaItemRequest> itens
) {
}
