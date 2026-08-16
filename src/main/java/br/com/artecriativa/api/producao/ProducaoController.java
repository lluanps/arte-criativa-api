package br.com.artecriativa.api.producao;

import br.com.artecriativa.api.producao.dto.ProducaoRequest;
import br.com.artecriativa.api.producao.dto.ProducaoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/producoes")
@RequiredArgsConstructor
public class ProducaoController {

    private final ProducaoService producaoService;

    @GetMapping
    public List<ProducaoResponse> listar() {
        return producaoService.listarTodas().stream().map(ProducaoResponse::de).toList();
    }

    @GetMapping("/{id}")
    public ProducaoResponse buscar(@PathVariable Long id) {
        return ProducaoResponse.de(producaoService.buscarPorId(id));
    }

    @GetMapping("/produto/{produtoId}")
    public List<ProducaoResponse> listarPorProduto(@PathVariable Long produtoId) {
        return producaoService.listarPorProduto(produtoId).stream().map(ProducaoResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProducaoResponse registrar(@Valid @RequestBody ProducaoRequest request) {
        return ProducaoResponse.de(producaoService.registrar(request));
    }
}
