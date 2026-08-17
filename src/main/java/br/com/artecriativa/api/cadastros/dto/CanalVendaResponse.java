package br.com.artecriativa.api.cadastros.dto;

import br.com.artecriativa.api.cadastros.CanalVenda;

import java.time.Instant;

public record CanalVendaResponse(
        Long id,
        String nome,
        Instant criadoEm
) {
    public static CanalVendaResponse de(CanalVenda canalVenda) {
        return new CanalVendaResponse(canalVenda.getId(), canalVenda.getNome(), canalVenda.getCriadoEm());
    }
}
