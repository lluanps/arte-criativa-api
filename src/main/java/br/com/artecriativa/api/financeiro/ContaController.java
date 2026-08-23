package br.com.artecriativa.api.financeiro;

import br.com.artecriativa.api.financeiro.dto.ContaParceladaRequest;
import br.com.artecriativa.api.financeiro.dto.ContaRequest;
import br.com.artecriativa.api.financeiro.dto.ContaResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/contas")
@RequiredArgsConstructor
public class ContaController {

    private final ContaService contaService;

    @GetMapping
    public List<ContaResponse> listar(@RequestParam(required = false) TipoConta tipo) {
        List<Conta> contas = tipo != null ? contaService.listarPorTipo(tipo) : contaService.listarTodas();
        return contas.stream().map(ContaResponse::de).toList();
    }

    @GetMapping("/{id}")
    public ContaResponse buscar(@PathVariable Long id) {
        return ContaResponse.de(contaService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContaResponse criar(@Valid @RequestBody ContaRequest request) {
        return ContaResponse.de(contaService.criar(request));
    }

    @PostMapping("/parceladas")
    @ResponseStatus(HttpStatus.CREATED)
    public List<ContaResponse> criarParcelada(@Valid @RequestBody ContaParceladaRequest request) {
        return contaService.criarParcelada(request).stream().map(ContaResponse::de).toList();
    }

    @PutMapping("/{id}")
    public ContaResponse atualizar(@PathVariable Long id, @Valid @RequestBody ContaRequest request) {
        return ContaResponse.de(contaService.atualizar(id, request));
    }

    @PostMapping("/{id}/pagar")
    public ContaResponse marcarComoPaga(@PathVariable Long id) {
        return ContaResponse.de(contaService.marcarComoPaga(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        contaService.excluir(id);
    }
}
