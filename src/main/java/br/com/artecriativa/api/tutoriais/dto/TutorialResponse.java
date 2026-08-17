package br.com.artecriativa.api.tutoriais.dto;

import br.com.artecriativa.api.tutoriais.Tutorial;

import java.time.Instant;
import java.util.List;

public record TutorialResponse(
        Long id,
        String titulo,
        String categoria,
        Long produtoRelacionadoId,
        String produtoRelacionadoNome,
        List<TutorialPassoResponse> passos,
        Instant criadoEm,
        Instant atualizadoEm
) {
    public static TutorialResponse de(Tutorial tutorial) {
        return new TutorialResponse(
                tutorial.getId(),
                tutorial.getTitulo(),
                tutorial.getCategoria(),
                tutorial.getProdutoRelacionado() != null ? tutorial.getProdutoRelacionado().getId() : null,
                tutorial.getProdutoRelacionado() != null ? tutorial.getProdutoRelacionado().getNome() : null,
                tutorial.getPassos().stream().map(TutorialPassoResponse::de).toList(),
                tutorial.getCriadoEm(),
                tutorial.getAtualizadoEm()
        );
    }
}
