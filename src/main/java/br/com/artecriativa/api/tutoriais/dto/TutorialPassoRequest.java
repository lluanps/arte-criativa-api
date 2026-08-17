package br.com.artecriativa.api.tutoriais.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TutorialPassoRequest(
        @NotNull(message = "ordem é obrigatória") Integer ordem,
        @NotBlank(message = "título é obrigatório") String titulo,
        String descricao,
        String midiaUrl
) {
}
