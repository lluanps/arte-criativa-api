package br.com.artecriativa.api.cadastros.dto;

import jakarta.validation.constraints.NotBlank;

public record CanalVendaRequest(
        @NotBlank(message = "nome é obrigatório") String nome
) {
}
