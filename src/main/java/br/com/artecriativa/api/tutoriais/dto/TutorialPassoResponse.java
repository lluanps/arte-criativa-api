package br.com.artecriativa.api.tutoriais.dto;

import br.com.artecriativa.api.tutoriais.TutorialPasso;

public record TutorialPassoResponse(
        Long id,
        Integer ordem,
        String titulo,
        String descricao,
        String midiaUrl
) {
    public static TutorialPassoResponse de(TutorialPasso passo) {
        return new TutorialPassoResponse(
                passo.getId(),
                passo.getOrdem(),
                passo.getTitulo(),
                passo.getDescricao(),
                passo.getMidiaUrl()
        );
    }
}
