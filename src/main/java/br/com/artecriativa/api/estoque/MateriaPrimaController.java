package br.com.artecriativa.api.estoque;

import br.com.artecriativa.api.estoque.dto.MateriaPrimaRequest;
import br.com.artecriativa.api.estoque.dto.MateriaPrimaResponse;
import br.com.artecriativa.api.estoque.dto.MovimentacaoMateriaPrimaRequest;
import br.com.artecriativa.api.estoque.dto.MovimentacaoResponse;
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
@RequestMapping("/api/materias-primas")
@RequiredArgsConstructor
public class MateriaPrimaController {

    private final MateriaPrimaService materiaPrimaService;

    @GetMapping
    public List<MateriaPrimaResponse> listar() {
        return materiaPrimaService.listarTodas().stream().map(MateriaPrimaResponse::de).toList();
    }

    @GetMapping("/{id}")
    public MateriaPrimaResponse buscar(@PathVariable Long id) {
        return MateriaPrimaResponse.de(materiaPrimaService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MateriaPrimaResponse criar(@Valid @RequestBody MateriaPrimaRequest request) {
        return MateriaPrimaResponse.de(materiaPrimaService.criar(request));
    }

    @PutMapping("/{id}")
    public MateriaPrimaResponse atualizar(@PathVariable Long id, @Valid @RequestBody MateriaPrimaRequest request) {
        return MateriaPrimaResponse.de(materiaPrimaService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        materiaPrimaService.excluir(id);
    }

    @PostMapping("/{id}/movimentacoes")
    @ResponseStatus(HttpStatus.CREATED)
    public MovimentacaoResponse registrarMovimentacao(@PathVariable Long id,
                                                        @Valid @RequestBody MovimentacaoMateriaPrimaRequest request) {
        return MovimentacaoResponse.de(materiaPrimaService.registrarMovimentacao(id, request));
    }

    @GetMapping("/{id}/movimentacoes")
    public List<MovimentacaoResponse> listarMovimentacoes(@PathVariable Long id) {
        return materiaPrimaService.listarMovimentacoes(id).stream().map(MovimentacaoResponse::de).toList();
    }
}
