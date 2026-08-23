package br.com.artecriativa.api.estoque.dto;

import jakarta.validation.constraints.NotBlank;

public record MateriaPrimaDesejadaRequest(
        @NotBlank(message = "nome é obrigatório") String nome
) {
}
