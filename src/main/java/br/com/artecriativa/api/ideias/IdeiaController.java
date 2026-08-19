package br.com.artecriativa.api.ideias;

import br.com.artecriativa.api.ideias.dto.IdeiaRequest;
import br.com.artecriativa.api.ideias.dto.IdeiaResponse;
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
@RequestMapping("/api/ideias")
@RequiredArgsConstructor
public class IdeiaController {

    private final IdeiaService ideiaService;

    @GetMapping
    public List<IdeiaResponse> listar() {
        return ideiaService.listarTodas().stream().map(IdeiaResponse::de).toList();
    }

    @GetMapping("/{id}")
    public IdeiaResponse buscar(@PathVariable Long id) {
        return IdeiaResponse.de(ideiaService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IdeiaResponse criar(@Valid @RequestBody IdeiaRequest request) {
        return IdeiaResponse.de(ideiaService.criar(request));
    }

    @PutMapping("/{id}")
    public IdeiaResponse atualizar(@PathVariable Long id, @Valid @RequestBody IdeiaRequest request) {
        return IdeiaResponse.de(ideiaService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        ideiaService.excluir(id);
    }
}
