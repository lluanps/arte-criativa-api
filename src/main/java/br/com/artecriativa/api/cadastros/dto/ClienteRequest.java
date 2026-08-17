package br.com.artecriativa.api.cadastros.dto;

import jakarta.validation.constraints.NotBlank;

public record ClienteRequest(
        @NotBlank(message = "nome é obrigatório") String nome,
        String telefone,
        String email
) {
}
