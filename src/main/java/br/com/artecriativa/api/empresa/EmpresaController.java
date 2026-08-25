package br.com.artecriativa.api.empresa;

import br.com.artecriativa.api.empresa.dto.EmpresaRequest;
import br.com.artecriativa.api.empresa.dto.EmpresaResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sem {@code POST}/{@code DELETE} de propósito — ver {@link EmpresaService}.
 */
@RestController
@RequestMapping("/api/empresa")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;

    @GetMapping
    public EmpresaResponse buscarAtual() {
        return EmpresaResponse.de(empresaService.buscarAtual());
    }

    @PutMapping
    public EmpresaResponse atualizar(@Valid @RequestBody EmpresaRequest request) {
        return EmpresaResponse.de(empresaService.atualizar(request));
    }
}
