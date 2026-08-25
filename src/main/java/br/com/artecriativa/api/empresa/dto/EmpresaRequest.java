package br.com.artecriativa.api.empresa.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmpresaRequest(
        @NotBlank(message = "nome é obrigatório") String nome,
        @Email(message = "email inválido") String email,
        String telefone,
        String cnpjOuCpf,
        String endereco,
        String logotipoUrl
) {
}
