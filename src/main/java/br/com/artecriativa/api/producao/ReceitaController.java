package br.com.artecriativa.api.producao;

import br.com.artecriativa.api.producao.dto.ReceitaRequest;
import br.com.artecriativa.api.producao.dto.ReceitaResponse;
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
@RequestMapping("/api/receitas")
@RequiredArgsConstructor
public class ReceitaController {

    private final ReceitaService receitaService;

    @GetMapping
    public List<ReceitaResponse> listar() {
        return receitaService.listarTodas().stream().map(ReceitaResponse::de).toList();
    }

    @GetMapping("/{id}")
    public ReceitaResponse buscar(@PathVariable Long id) {
        return ReceitaResponse.de(receitaService.buscarPorId(id));
    }

    @GetMapping("/produto/{produtoId}")
    public ReceitaResponse buscarPorProduto(@PathVariable Long produtoId) {
        return ReceitaResponse.de(receitaService.buscarPorProdutoId(produtoId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReceitaResponse criar(@Valid @RequestBody ReceitaRequest request) {
        return ReceitaResponse.de(receitaService.criar(request));
    }

    @PutMapping("/{id}")
    public ReceitaResponse atualizar(@PathVariable Long id, @Valid @RequestBody ReceitaRequest request) {
        return ReceitaResponse.de(receitaService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        receitaService.excluir(id);
    }
}
