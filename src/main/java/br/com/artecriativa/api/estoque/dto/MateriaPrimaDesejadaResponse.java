package br.com.artecriativa.api.estoque.dto;

import br.com.artecriativa.api.estoque.MateriaPrimaDesejada;

import java.time.Instant;

public record MateriaPrimaDesejadaResponse(
        Long id,
        String nome,
        Instant criadoEm
) {
    public static MateriaPrimaDesejadaResponse de(MateriaPrimaDesejada desejada) {
        return new MateriaPrimaDesejadaResponse(desejada.getId(), desejada.getNome(), desejada.getCriadoEm());
    }
}
