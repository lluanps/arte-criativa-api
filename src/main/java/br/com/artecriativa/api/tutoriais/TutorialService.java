package br.com.artecriativa.api.tutoriais;

import br.com.artecriativa.api.common.RecursoNaoEncontradoException;
import br.com.artecriativa.api.estoque.Produto;
import br.com.artecriativa.api.estoque.ProdutoRepository;
import br.com.artecriativa.api.tutoriais.dto.TutorialPassoRequest;
import br.com.artecriativa.api.tutoriais.dto.TutorialRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TutorialService {

    private final TutorialRepository tutorialRepository;
    private final ProdutoRepository produtoRepository;

    @Transactional(readOnly = true)
    public List<Tutorial> listarTodos() {
        return tutorialRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Tutorial buscarPorId(Long id) {
        return tutorialRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tutorial não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public List<Tutorial> listarPorProduto(Long produtoId) {
        return tutorialRepository.findByProdutoRelacionadoId(produtoId);
    }

    @Transactional
    public Tutorial criar(TutorialRequest request) {
        Tutorial tutorial = new Tutorial();
        aplicarRequest(tutorial, request);
        return tutorialRepository.save(tutorial);
    }

    @Transactional
    public Tutorial atualizar(Long id, TutorialRequest request) {
        Tutorial tutorial = buscarPorId(id);
        aplicarRequest(tutorial, request);
        return tutorialRepository.save(tutorial);
    }

    @Transactional
    public void excluir(Long id) {
        Tutorial tutorial = buscarPorId(id);
        tutorialRepository.delete(tutorial);
    }

    private void aplicarRequest(Tutorial tutorial, TutorialRequest request) {
        tutorial.setTitulo(request.titulo());
        tutorial.setCategoria(request.categoria());

        if (request.produtoRelacionadoId() != null) {
            Produto produto = produtoRepository.findById(request.produtoRelacionadoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Produto não encontrado: " + request.produtoRelacionadoId()));
            tutorial.setProdutoRelacionado(produto);
        } else {
            tutorial.setProdutoRelacionado(null);
        }

        List<TutorialPasso> passos = request.passos().stream().map(this::criarPasso).toList();
        tutorial.substituirPassos(passos);
    }

    private TutorialPasso criarPasso(TutorialPassoRequest passoRequest) {
        TutorialPasso passo = new TutorialPasso();
        passo.setOrdem(passoRequest.ordem());
        passo.setTitulo(passoRequest.titulo());
        passo.setDescricao(passoRequest.descricao());
        passo.setMidiaUrl(passoRequest.midiaUrl());
        return passo;
    }
}
