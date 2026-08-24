package br.com.artecriativa.api.cadastros;

import br.com.artecriativa.api.cadastros.dto.FornecedorRequest;
import br.com.artecriativa.api.common.RecursoNaoEncontradoException;
import br.com.artecriativa.api.estoque.MateriaPrimaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FornecedorService {

    private final FornecedorRepository fornecedorRepository;
    private final MateriaPrimaRepository materiaPrimaRepository;

    @Transactional(readOnly = true)
    public List<Fornecedor> listarTodos() {
        return fornecedorRepository.findAllByOrderByNome();
    }

    @Transactional(readOnly = true)
    public Fornecedor buscarPorId(Long id) {
        return fornecedorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Fornecedor não encontrado: " + id));
    }

    @Transactional
    public Fornecedor criar(FornecedorRequest request) {
        Fornecedor fornecedor = new Fornecedor();
        aplicarRequest(fornecedor, request);
        return fornecedorRepository.save(fornecedor);
    }

    @Transactional
    public Fornecedor atualizar(Long id, FornecedorRequest request) {
        Fornecedor fornecedor = buscarPorId(id);
        aplicarRequest(fornecedor, request);
        return fornecedorRepository.save(fornecedor);
    }

    @Transactional
    public void excluir(Long id) {
        Fornecedor fornecedor = buscarPorId(id);
        long vinculadas = materiaPrimaRepository.countByFornecedorId(id);
        if (vinculadas > 0) {
            throw new IllegalStateException(
                    "Não é possível excluir '%s': %s vinculada(s) a este fornecedor."
                            .formatted(fornecedor.getNome(), vinculadas == 1 ? "1 matéria-prima está" : vinculadas + " matérias-primas estão"));
        }
        fornecedorRepository.delete(fornecedor);
    }

    private void aplicarRequest(Fornecedor fornecedor, FornecedorRequest request) {
        fornecedor.setNome(request.nome().trim());
        fornecedor.setTelefone(request.telefone());
        fornecedor.setObservacao(request.observacao());
    }
}
