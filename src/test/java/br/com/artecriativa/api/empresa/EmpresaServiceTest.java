package br.com.artecriativa.api.empresa;

import br.com.artecriativa.api.common.RecursoNaoEncontradoException;
import br.com.artecriativa.api.empresa.dto.EmpresaRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @InjectMocks
    private EmpresaService service;

    private static Empresa empresaExistente(Long id, String nome) {
        Empresa empresa = new Empresa();
        empresa.setId(id);
        empresa.setNome(nome);
        return empresa;
    }

    @AfterEach
    void limparTenantContext() {
        TenantContext.clear();
    }

    @Test
    void buscarAtual_usaEmpresaDoTenantContext() {
        TenantContext.set(1L);
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresaExistente(1L, "Arte Criativa")));

        Empresa empresa = service.buscarAtual();

        assertThat(empresa.getNome()).isEqualTo("Arte Criativa");
    }

    @Test
    void buscarAtual_semEmpresaCorrespondente_lancaExcecao() {
        TenantContext.set(99L);
        when(empresaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarAtual())
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void atualizar_removeEspacosDoNomeEAplicaDemaisCampos() {
        TenantContext.set(1L);
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresaExistente(1L, "Nome antigo")));
        when(empresaRepository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(inv -> inv.getArgument(0));

        EmpresaRequest request = new EmpresaRequest("  Nome Novo  ", "contato@empresa.com",
                "11999998888", "12.345.678/0001-90", "Rua X, 123", "https://exemplo.com/logo.png");

        Empresa atualizada = service.atualizar(request);

        assertThat(atualizada.getNome()).isEqualTo("Nome Novo");
        assertThat(atualizada.getEmail()).isEqualTo("contato@empresa.com");
        assertThat(atualizada.getTelefone()).isEqualTo("11999998888");
        assertThat(atualizada.getCnpjOuCpf()).isEqualTo("12.345.678/0001-90");
        assertThat(atualizada.getEndereco()).isEqualTo("Rua X, 123");
        assertThat(atualizada.getLogotipoUrl()).isEqualTo("https://exemplo.com/logo.png");
    }
}
