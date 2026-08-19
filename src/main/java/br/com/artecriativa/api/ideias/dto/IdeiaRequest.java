package br.com.artecriativa.api.ideias.dto;

import br.com.artecriativa.api.ideias.StatusIdeia;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record IdeiaRequest(
        @NotBlank(message = "título é obrigatório") String titulo,
        String corpo,
        StatusIdeia status,
        Boolean favorita,
        Long produtoRelacionadoId,
        List<String> tags,
        List<String> fotosUrls
) {
    public IdeiaRequest {
        if (status == null) {
            status = StatusIdeia.IDEIA_SOLTA;
        }
        if (favorita == null) {
            favorita = false;
        }
        if (tags == null) {
            tags = List.of();
        }
        if (fotosUrls == null) {
            fotosUrls = List.of();
        }
    }
}
