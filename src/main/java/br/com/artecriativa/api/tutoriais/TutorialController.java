package br.com.artecriativa.api.tutoriais;

import br.com.artecriativa.api.tutoriais.dto.TutorialRequest;
import br.com.artecriativa.api.tutoriais.dto.TutorialResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tutoriais")
@RequiredArgsConstructor
public class TutorialController {

    private final TutorialService tutorialService;

    @GetMapping
    public List<TutorialResponse> listar() {
        return tutorialService.listarTodos().stream().map(TutorialResponse::de).toList();
    }

    @GetMapping("/{id}")
    public TutorialResponse buscar(@PathVariable Long id) {
        return TutorialResponse.de(tutorialService.buscarPorId(id));
    }

    @GetMapping("/produto/{produtoId}")
    public List<TutorialResponse> listarPorProduto(@PathVariable Long produtoId) {
        return tutorialService.listarPorProduto(produtoId).stream().map(TutorialResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TutorialResponse criar(@Valid @RequestBody TutorialRequest request) {
        return TutorialResponse.de(tutorialService.criar(request));
    }

    @PutMapping("/{id}")
    public TutorialResponse atualizar(@PathVariable Long id, @Valid @RequestBody TutorialRequest request) {
        return TutorialResponse.de(tutorialService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        tutorialService.excluir(id);
    }
}
