package br.com.artecriativa.api.cadastros.dto;

import br.com.artecriativa.api.cadastros.Cliente;

import java.time.Instant;

public record ClienteResponse(
        Long id,
        String nome,
        String telefone,
        String email,
        Instant criadoEm
) {
    public static ClienteResponse de(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getTelefone(),
                cliente.getEmail(),
                cliente.getCriadoEm()
        );
    }
}
