package br.com.artecriativa.api.cadastros.dto;

import br.com.artecriativa.api.cadastros.Fornecedor;

import java.time.Instant;

public record FornecedorResponse(
        Long id,
        String nome,
        String telefone,
        String observacao,
        Instant criadoEm
) {
    public static FornecedorResponse de(Fornecedor fornecedor) {
        return new FornecedorResponse(
                fornecedor.getId(),
                fornecedor.getNome(),
                fornecedor.getTelefone(),
                fornecedor.getObservacao(),
                fornecedor.getCriadoEm()
        );
    }
}
