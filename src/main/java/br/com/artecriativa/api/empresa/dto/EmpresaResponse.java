package br.com.artecriativa.api.empresa.dto;

import br.com.artecriativa.api.empresa.Empresa;

public record EmpresaResponse(
        Long id,
        String nome,
        String email,
        String telefone,
        String cnpjOuCpf,
        String endereco,
        String logotipoUrl
) {
    public static EmpresaResponse de(Empresa empresa) {
        return new EmpresaResponse(
                empresa.getId(),
                empresa.getNome(),
                empresa.getEmail(),
                empresa.getTelefone(),
                empresa.getCnpjOuCpf(),
                empresa.getEndereco(),
                empresa.getLogotipoUrl()
        );
    }
}
