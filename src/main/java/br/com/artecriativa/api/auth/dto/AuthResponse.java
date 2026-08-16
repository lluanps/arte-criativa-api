package br.com.artecriativa.api.auth.dto;

public record AuthResponse(
        String token,
        Long usuarioId,
        String nome,
        String email
) {
}
