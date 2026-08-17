package br.com.artecriativa.api.cadastros;

import br.com.artecriativa.api.cadastros.dto.CanalVendaRequest;
import br.com.artecriativa.api.cadastros.dto.CanalVendaResponse;
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
@RequestMapping("/api/canais-venda")
@RequiredArgsConstructor
public class CanalVendaController {

    private final CanalVendaService canalVendaService;

    @GetMapping
    public List<CanalVendaResponse> listar() {
        return canalVendaService.listarTodos().stream().map(CanalVendaResponse::de).toList();
    }

    @GetMapping("/{id}")
    public CanalVendaResponse buscar(@PathVariable Long id) {
        return CanalVendaResponse.de(canalVendaService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CanalVendaResponse criar(@Valid @RequestBody CanalVendaRequest request) {
        return CanalVendaResponse.de(canalVendaService.criar(request));
    }

    @PutMapping("/{id}")
    public CanalVendaResponse atualizar(@PathVariable Long id, @Valid @RequestBody CanalVendaRequest request) {
        return CanalVendaResponse.de(canalVendaService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        canalVendaService.excluir(id);
    }
}
