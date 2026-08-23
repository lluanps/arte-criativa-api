package br.com.artecriativa.api.cadastros;

import br.com.artecriativa.api.cadastros.dto.CategoriaMateriaPrimaRequest;
import br.com.artecriativa.api.cadastros.dto.CategoriaMateriaPrimaResponse;
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
@RequestMapping("/api/categorias-materia-prima")
@RequiredArgsConstructor
public class CategoriaMateriaPrimaController {

    private final CategoriaMateriaPrimaService categoriaService;

    @GetMapping
    public List<CategoriaMateriaPrimaResponse> listar() {
        return categoriaService.listarTodas().stream().map(CategoriaMateriaPrimaResponse::de).toList();
    }

    @GetMapping("/{id}")
    public CategoriaMateriaPrimaResponse buscar(@PathVariable Long id) {
        return CategoriaMateriaPrimaResponse.de(categoriaService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoriaMateriaPrimaResponse criar(@Valid @RequestBody CategoriaMateriaPrimaRequest request) {
        return CategoriaMateriaPrimaResponse.de(categoriaService.criar(request));
    }

    @PutMapping("/{id}")
    public CategoriaMateriaPrimaResponse atualizar(@PathVariable Long id, @Valid @RequestBody CategoriaMateriaPrimaRequest request) {
        return CategoriaMateriaPrimaResponse.de(categoriaService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        categoriaService.excluir(id);
    }
}
