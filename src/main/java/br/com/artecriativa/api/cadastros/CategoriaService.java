package br.com.artecriativa.api.cadastros;

import br.com.artecriativa.api.cadastros.dto.CategoriaRequest;
import br.com.artecriativa.api.cadastros.dto.PrecoMercadoRequest;
import br.com.artecriativa.api.common.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Transactional(readOnly = true)
    public List<Categoria> listarTodas() {
        return categoriaRepository.findAllByOrderByNome();
    }

    @Transactional(readOnly = true)
    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada: " + id));
    }

    @Transactional
    public Categoria criar(CategoriaRequest request) {
        String nome = request.nome().trim();
        if (categoriaRepository.existsByNomeIgnoreCase(nome)) {
            throw new IllegalStateException("Já existe uma categoria com esse nome");
        }
        Categoria categoria = new Categoria();
        categoria.setNome(nome);
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public Categoria atualizar(Long id, CategoriaRequest request) {
        Categoria categoria = buscarPorId(id);
        String nome = request.nome().trim();
        if (!nome.equalsIgnoreCase(categoria.getNome()) && categoriaRepository.existsByNomeIgnoreCase(nome)) {
            throw new IllegalStateException("Já existe uma categoria com esse nome");
        }
        categoria.setNome(nome);
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public void excluir(Long id) {
        Categoria categoria = buscarPorId(id);
        categoriaRepository.delete(categoria);
    }

    /**
     * Grava a faixa de preço de mercado pra produtos parecidos, pesquisada manualmente
     * (ver {@link Categoria#getPrecoMercadoAtualizadoEm()}) — não é busca automática.
     */
    @Transactional
    public Categoria atualizarPrecoMercado(Long id, PrecoMercadoRequest request) {
        Categoria categoria = buscarPorId(id);
        if (request.min().compareTo(request.max()) > 0) {
            throw new IllegalStateException("Preço mínimo não pode ser maior que o máximo");
        }
        categoria.setPrecoMercadoMin(request.min());
        categoria.setPrecoMercadoMax(request.max());
        categoria.setPrecoMercadoAtualizadoEm(Instant.now());
        return categoriaRepository.save(categoria);
    }
}
