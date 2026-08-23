package br.com.artecriativa.api.cadastros.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoriaMateriaPrimaRequest(
        @NotBlank(message = "nome é obrigatório") String nome
) {
}
