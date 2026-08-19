package br.com.artecriativa.api.ideias;

import br.com.artecriativa.api.common.RecursoNaoEncontradoException;
import br.com.artecriativa.api.estoque.Produto;
import br.com.artecriativa.api.estoque.ProdutoRepository;
import br.com.artecriativa.api.ideias.dto.IdeiaRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IdeiaService {

    private final IdeiaRepository ideiaRepository;
    private final ProdutoRepository produtoRepository;

    @Transactional(readOnly = true)
    public List<Ideia> listarTodas() {
        return ideiaRepository.findAllByOrderByFavoritaDescAtualizadoEmDesc();
    }

    @Transactional(readOnly = true)
    public Ideia buscarPorId(Long id) {
        return ideiaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ideia não encontrada: " + id));
    }

    @Transactional
    public Ideia criar(IdeiaRequest request) {
        Ideia ideia = new Ideia();
        aplicarRequest(ideia, request);
        return ideiaRepository.save(ideia);
    }

    @Transactional
    public Ideia atualizar(Long id, IdeiaRequest request) {
        Ideia ideia = buscarPorId(id);
        aplicarRequest(ideia, request);
        return ideiaRepository.save(ideia);
    }

    /** Ideia é uma anotação solta — nada referencia ela de volta, então nunca é bloqueada. */
    @Transactional
    public void excluir(Long id) {
        Ideia ideia = buscarPorId(id);
        ideiaRepository.delete(ideia);
    }

    private void aplicarRequest(Ideia ideia, IdeiaRequest request) {
        ideia.setTitulo(request.titulo());
        ideia.setCorpo(request.corpo());
        ideia.setStatus(request.status());
        ideia.setFavorita(request.favorita());
        ideia.setProdutoRelacionado(buscarProduto(request.produtoRelacionadoId()));
        ideia.setTags(new ArrayList<>(request.tags()));
        ideia.setFotosUrls(new ArrayList<>(request.fotosUrls()));
    }

    private Produto buscarProduto(Long produtoId) {
        if (produtoId == null) {
            return null;
        }
        return produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado: " + produtoId));
    }
}
