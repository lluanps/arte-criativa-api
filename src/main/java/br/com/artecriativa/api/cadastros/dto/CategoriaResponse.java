package br.com.artecriativa.api.cadastros.dto;

import br.com.artecriativa.api.cadastros.Categoria;

import java.math.BigDecimal;
import java.time.Instant;

public record CategoriaResponse(
        Long id,
        String nome,
        BigDecimal precoMercadoMin,
        BigDecimal precoMercadoMax,
        Instant precoMercadoAtualizadoEm,
        Instant criadoEm
) {
    public static CategoriaResponse de(Categoria categoria) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNome(),
                categoria.getPrecoMercadoMin(),
                categoria.getPrecoMercadoMax(),
                categoria.getPrecoMercadoAtualizadoEm(),
                categoria.getCriadoEm()
        );
    }
}
