package br.com.artecriativa.api.vendas;

import br.com.artecriativa.api.vendas.dto.ReagendarEntregaRequest;
import br.com.artecriativa.api.vendas.dto.VendaRequest;
import br.com.artecriativa.api.vendas.dto.VendaResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vendas")
@RequiredArgsConstructor
public class VendaController {

    private final VendaService vendaService;

    @GetMapping
    public List<VendaResponse> listar() {
        return vendaService.listarTodas().stream().map(VendaResponse::de).toList();
    }

    @GetMapping("/{id}")
    public VendaResponse buscar(@PathVariable Long id) {
        return VendaResponse.de(vendaService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VendaResponse registrar(@Valid @RequestBody VendaRequest request) {
        return VendaResponse.de(vendaService.registrar(request));
    }

    @GetMapping("/cliente/{clienteId}")
    public List<VendaResponse> listarPorCliente(@PathVariable Long clienteId) {
        return vendaService.listarPorCliente(clienteId).stream().map(VendaResponse::de).toList();
    }

    /** Avança a encomenda pro próximo estágio de status — ver {@link VendaService#avancarStatus}. */
    @PostMapping("/{id}/avancar-status")
    public VendaResponse avancarStatus(@PathVariable Long id) {
        return VendaResponse.de(vendaService.avancarStatus(id));
    }

    /** Reagenda a data de entrega combinada de uma encomenda. */
    @PostMapping("/{id}/reagendar-entrega")
    public VendaResponse reagendarEntrega(@PathVariable Long id, @Valid @RequestBody ReagendarEntregaRequest request) {
        return VendaResponse.de(vendaService.reagendarEntrega(id, request.novaDataEntrega()));
    }

    /** Estorna estoque e remove o(s) lançamento(s) financeiro(s) gerado(s) — ver {@link VendaService#excluir}. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        vendaService.excluir(id);
    }
}
