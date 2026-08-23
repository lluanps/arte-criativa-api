package br.com.artecriativa.api.cadastros.dto;

import br.com.artecriativa.api.cadastros.CategoriaMateriaPrima;

import java.time.Instant;

public record CategoriaMateriaPrimaResponse(
        Long id,
        String nome,
        Instant criadoEm
) {
    public static CategoriaMateriaPrimaResponse de(CategoriaMateriaPrima categoria) {
        return new CategoriaMateriaPrimaResponse(categoria.getId(), categoria.getNome(), categoria.getCriadoEm());
    }
}
