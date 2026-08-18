package br.com.artecriativa.api.cadastros;

import br.com.artecriativa.api.cadastros.dto.CategoriaRequest;
import br.com.artecriativa.api.cadastros.dto.CategoriaResponse;
import br.com.artecriativa.api.cadastros.dto.PrecoMercadoRequest;
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
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping
    public List<CategoriaResponse> listar() {
        return categoriaService.listarTodas().stream().map(CategoriaResponse::de).toList();
    }

    @GetMapping("/{id}")
    public CategoriaResponse buscar(@PathVariable Long id) {
        return CategoriaResponse.de(categoriaService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoriaResponse criar(@Valid @RequestBody CategoriaRequest request) {
        return CategoriaResponse.de(categoriaService.criar(request));
    }

    @PutMapping("/{id}")
    public CategoriaResponse atualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest request) {
        return CategoriaResponse.de(categoriaService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        categoriaService.excluir(id);
    }

    /** Grava a faixa de preço de mercado pesquisada manualmente — não busca nada sozinho. */
    @PutMapping("/{id}/preco-mercado")
    public CategoriaResponse atualizarPrecoMercado(@PathVariable Long id, @Valid @RequestBody PrecoMercadoRequest request) {
        return CategoriaResponse.de(categoriaService.atualizarPrecoMercado(id, request));
    }
}
