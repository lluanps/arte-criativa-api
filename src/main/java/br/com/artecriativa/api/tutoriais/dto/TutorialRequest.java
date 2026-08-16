package br.com.artecriativa.api.tutoriais.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record TutorialRequest(
        @NotBlank(message = "título é obrigatório") String titulo,
        String categoria,
        Long produtoRelacionadoId,
        @NotEmpty(message = "tutorial precisa de ao menos um passo")
        List<@Valid TutorialPassoRequest> passos
) {
}
