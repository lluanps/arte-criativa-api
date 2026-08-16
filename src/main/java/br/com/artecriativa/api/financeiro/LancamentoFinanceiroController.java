package br.com.artecriativa.api.financeiro;

import br.com.artecriativa.api.financeiro.dto.LancamentoFinanceiroRequest;
import br.com.artecriativa.api.financeiro.dto.LancamentoFinanceiroResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/lancamentos-financeiros")
@RequiredArgsConstructor
public class LancamentoFinanceiroController {

    private final LancamentoFinanceiroService lancamentoFinanceiroService;

    @GetMapping
    public List<LancamentoFinanceiroResponse> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        List<LancamentoFinanceiro> lancamentos = (inicio != null && fim != null)
                ? lancamentoFinanceiroService.listarPorPeriodo(inicio, fim)
                : lancamentoFinanceiroService.listarTodos();
        return lancamentos.stream().map(LancamentoFinanceiroResponse::de).toList();
    }

    @GetMapping("/{id}")
    public LancamentoFinanceiroResponse buscar(@PathVariable Long id) {
        return LancamentoFinanceiroResponse.de(lancamentoFinanceiroService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LancamentoFinanceiroResponse criar(@Valid @RequestBody LancamentoFinanceiroRequest request) {
        return LancamentoFinanceiroResponse.de(lancamentoFinanceiroService.criar(request));
    }

    @PutMapping("/{id}")
    public LancamentoFinanceiroResponse atualizar(@PathVariable Long id,
                                                    @Valid @RequestBody LancamentoFinanceiroRequest request) {
        return LancamentoFinanceiroResponse.de(lancamentoFinanceiroService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        lancamentoFinanceiroService.excluir(id);
    }
}
