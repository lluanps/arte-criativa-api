package br.com.artecriativa.api.estoque;

import br.com.artecriativa.api.estoque.dto.MateriaPrimaDesejadaRequest;
import br.com.artecriativa.api.estoque.dto.MateriaPrimaDesejadaResponse;
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
@RequestMapping("/api/materias-primas/desejadas")
@RequiredArgsConstructor
public class MateriaPrimaDesejadaController {

    private final MateriaPrimaDesejadaService service;

    @GetMapping
    public List<MateriaPrimaDesejadaResponse> listar() {
        return service.listarTodas().stream().map(MateriaPrimaDesejadaResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MateriaPrimaDesejadaResponse criar(@Valid @RequestBody MateriaPrimaDesejadaRequest request) {
        return MateriaPrimaDesejadaResponse.de(service.criar(request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
