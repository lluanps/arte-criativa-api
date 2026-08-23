package br.com.artecriativa.api.cadastros;

import br.com.artecriativa.api.cadastros.dto.CategoriaMateriaPrimaRequest;
import br.com.artecriativa.api.common.RecursoNaoEncontradoException;
import br.com.artecriativa.api.estoque.MateriaPrimaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaMateriaPrimaService {

    private final CategoriaMateriaPrimaRepository categoriaRepository;
    private final MateriaPrimaRepository materiaPrimaRepository;

    @Transactional(readOnly = true)
    public List<CategoriaMateriaPrima> listarTodas() {
        return categoriaRepository.findAllByOrderByNome();
    }

    @Transactional(readOnly = true)
    public CategoriaMateriaPrima buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria de matéria-prima não encontrada: " + id));
    }

    @Transactional
    public CategoriaMateriaPrima criar(CategoriaMateriaPrimaRequest request) {
        String nome = request.nome().trim();
        if (categoriaRepository.existsByNomeIgnoreCase(nome)) {
            throw new IllegalStateException("Já existe uma categoria de matéria-prima com esse nome");
        }
        CategoriaMateriaPrima categoria = new CategoriaMateriaPrima();
        categoria.setNome(nome);
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public CategoriaMateriaPrima atualizar(Long id, CategoriaMateriaPrimaRequest request) {
        CategoriaMateriaPrima categoria = buscarPorId(id);
        String nome = request.nome().trim();
        if (!nome.equalsIgnoreCase(categoria.getNome()) && categoriaRepository.existsByNomeIgnoreCase(nome)) {
            throw new IllegalStateException("Já existe uma categoria de matéria-prima com esse nome");
        }
        categoria.setNome(nome);
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public void excluir(Long id) {
        CategoriaMateriaPrima categoria = buscarPorId(id);
        long vinculadas = materiaPrimaRepository.countByCategoriaId(id);
        if (vinculadas > 0) {
            throw new IllegalStateException(
                    "Não é possível excluir '%s': %s vinculada(s) a esta categoria."
                            .formatted(categoria.getNome(), vinculadas == 1 ? "1 matéria-prima está" : vinculadas + " matérias-primas estão"));
        }
        categoriaRepository.delete(categoria);
    }
}
