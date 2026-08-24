package br.com.artecriativa.api.cadastros;

import br.com.artecriativa.api.cadastros.dto.FornecedorRequest;
import br.com.artecriativa.api.common.RecursoNaoEncontradoException;
import br.com.artecriativa.api.estoque.MateriaPrimaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FornecedorServiceTest {

    @Mock
    private FornecedorRepository fornecedorRepository;
    @Mock
    private MateriaPrimaRepository materiaPrimaRepository;

    @InjectMocks
    private FornecedorService service;

    private static Fornecedor fornecedorExistente(Long id, String nome) {
        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setId(id);
        fornecedor.setNome(nome);
        return fornecedor;
    }

    @Test
    void criar_removeEspacosDoNome() {
        when(fornecedorRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        FornecedorRequest request = new FornecedorRequest("  Mercado Livre  ", "11999998888", "loja online");

        Fornecedor criado = service.criar(request);

        assertThat(criado.getNome()).isEqualTo("Mercado Livre");
        assertThat(criado.getTelefone()).isEqualTo("11999998888");
        assertThat(criado.getObservacao()).isEqualTo("loja online");
    }

    @Test
    void buscarPorId_inexistente_lancaExcecao() {
        when(fornecedorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void excluir_comMateriaPrimaVinculada_lancaExcecaoENaoExclui() {
        Fornecedor fornecedor = fornecedorExistente(1L, "Fornecedor X");
        when(fornecedorRepository.findById(1L)).thenReturn(Optional.of(fornecedor));
        when(materiaPrimaRepository.countByFornecedorId(1L)).thenReturn(3L);

        assertThatThrownBy(() -> service.excluir(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("3 matérias-primas estão");

        verify(fornecedorRepository, never()).delete(any());
    }

    @Test
    void excluir_semVinculo_excluiNormalmente() {
        Fornecedor fornecedor = fornecedorExistente(1L, "Fornecedor X");
        when(fornecedorRepository.findById(1L)).thenReturn(Optional.of(fornecedor));
        when(materiaPrimaRepository.countByFornecedorId(1L)).thenReturn(0L);

        service.excluir(1L);

        verify(fornecedorRepository).delete(fornecedor);
    }
}
