package br.com.artecriativa.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EsqueciSenhaRequest(
        @NotBlank(message = "email é obrigatório") @Email(message = "email inválido") String email
) {
}
