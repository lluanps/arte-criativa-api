package br.com.artecriativa.api.empresa;

import br.com.artecriativa.api.common.RecursoNaoEncontradoException;
import br.com.artecriativa.api.empresa.dto.EmpresaRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Não tem {@code criar}/{@code excluir} aqui de propósito — criar uma empresa nova (com o
 * primeiro usuário dela) é fluxo público, sem tenant resolvido ainda, então vive em
 * {@code AuthService#registrarEmpresa} ({@code POST /api/auth/registrar-empresa}), não
 * aqui. Este service só lida com "a própria empresa" de quem já está autenticado,
 * resolvida via {@link TenantContext} — não tem {@code excluir} nenhum, empresa não se
 * autoexclui.
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
