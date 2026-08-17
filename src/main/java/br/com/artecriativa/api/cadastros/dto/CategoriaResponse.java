package br.com.artecriativa.api.cadastros.dto;

import br.com.artecriativa.api.cadastros.Categoria;

import java.time.Instant;

public record CategoriaResponse(
        Long id,
        String nome,
        Instant criadoEm
) {
    public static CategoriaResponse de(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNome(), categoria.getCriadoEm());
    }
}
