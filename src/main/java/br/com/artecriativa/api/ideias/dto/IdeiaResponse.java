package br.com.artecriativa.api.ideias.dto;

import br.com.artecriativa.api.ideias.Ideia;
import br.com.artecriativa.api.ideias.StatusIdeia;

import java.time.Instant;
import java.util.List;

public record IdeiaResponse(
        Long id,
        String titulo,
        String corpo,
        StatusIdeia status,
        boolean favorita,
        Long produtoRelacionadoId,
        String produtoRelacionadoNome,
        List<String> tags,
        List<String> fotosUrls,
        Instant criadoEm,
        Instant atualizadoEm
) {
    public static IdeiaResponse de(Ideia ideia) {
        return new IdeiaResponse(
                ideia.getId(),
                ideia.getTitulo(),
                ideia.getCorpo(),
                ideia.getStatus(),
                ideia.isFavorita(),
                ideia.getProdutoRelacionado() != null ? ideia.getProdutoRelacionado().getId() : null,
                ideia.getProdutoRelacionado() != null ? ideia.getProdutoRelacionado().getNome() : null,
                ideia.getTags(),
                ideia.getFotosUrls(),
                ideia.getCriadoEm(),
                ideia.getAtualizadoEm()
        );
    }
}
