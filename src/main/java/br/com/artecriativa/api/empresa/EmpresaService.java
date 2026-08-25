package br.com.artecriativa.api.empresa;

import br.com.artecriativa.api.common.RecursoNaoEncontradoException;
import br.com.artecriativa.api.empresa.dto.EmpresaRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Não tem {@code criar}/{@code excluir} de propósito: empresa nova nasce por SQL direto
 * (piloto manual, poucos clientes de confiança) até existir cadastro self-service — fase
 * futura, pausada. Só existe "a própria empresa", resolvida via {@link TenantContext}.
 */
@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    @Transactional(readOnly = true)
    public Empresa buscarAtual() {
        return empresaRepository.findById(TenantContext.get())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa não encontrada"));
    }

    @Transactional
    public Empresa atualizar(EmpresaRequest request) {
        Empresa empresa = buscarAtual();
        empresa.setNome(request.nome().trim());
        empresa.setEmail(request.email());
        empresa.setTelefone(request.telefone());
        empresa.setCnpjOuCpf(request.cnpjOuCpf());
        empresa.setEndereco(request.endereco());
        empresa.setLogotipoUrl(request.logotipoUrl());
        return empresaRepository.save(empresa);
    }
}
