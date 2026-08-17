package br.com.artecriativa.api.cadastros.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoriaRequest(
        @NotBlank(message = "nome é obrigatório") String nome
) {
}
