package br.com.artecriativa.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Corpo do cadastro público ({@code POST /api/auth/registrar-empresa}) — cria a
 * {@link br.com.artecriativa.api.empresa.Empresa} e o primeiro
 * {@link br.com.artecriativa.api.auth.Usuario} dela numa tacada só. Os demais campos de
 * empresa (telefone, CNPJ/CPF, endereço, logotipo) ficam de fora de propósito — dá pra
 * completar depois via {@code PUT /api/empresa}, já autenticado.
 */
public record RegistroEmpresaRequest(
        @NotBlank(message = "nome da empresa é obrigatório") String nomeEmpresa,
        @NotBlank(message = "nome é obrigatório") String nome,
        @NotBlank(message = "email é obrigatório") @Email(message = "email inválido") String email,
        @NotBlank(message = "senha é obrigatória")
        @Size(min = 8, message = "senha deve ter ao menos 8 caracteres") String senha
) {
}
