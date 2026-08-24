package br.com.artecriativa.api.cadastros.dto;

import jakarta.validation.constraints.NotBlank;

public record FornecedorRequest(
        @NotBlank(message = "nome é obrigatório") String nome,
        String telefone,
        String observacao
) {
}
