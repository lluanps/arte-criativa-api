package br.com.artecriativa.api.cadastros;

import br.com.artecriativa.api.cadastros.dto.FornecedorRequest;
import br.com.artecriativa.api.cadastros.dto.FornecedorResponse;
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
@RequestMapping("/api/fornecedores")
@RequiredArgsConstructor
public class FornecedorController {

    private final FornecedorService fornecedorService;

    @GetMapping
    public List<FornecedorResponse> listar() {
        return fornecedorService.listarTodos().stream().map(FornecedorResponse::de).toList();
    }

    @GetMapping("/{id}")
    public FornecedorResponse buscar(@PathVariable Long id) {
        return FornecedorResponse.de(fornecedorService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FornecedorResponse criar(@Valid @RequestBody FornecedorRequest request) {
        return FornecedorResponse.de(fornecedorService.criar(request));
    }

    @PutMapping("/{id}")
    public FornecedorResponse atualizar(@PathVariable Long id, @Valid @RequestBody FornecedorRequest request) {
        return FornecedorResponse.de(fornecedorService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        fornecedorService.excluir(id);
    }
}
