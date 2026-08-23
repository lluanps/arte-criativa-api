package br.com.artecriativa.api.estoque;

import br.com.artecriativa.api.common.PaginaResponse;
import br.com.artecriativa.api.estoque.dto.MateriaPrimaAtualizacaoRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
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

    /**
     * Busca paginada com filtros — usada pela tela de listagem de matérias-primas.
     * Diferente de {@code listar()} acima (lista inteira, usada pelo seletor de
     * matéria-prima em Fichas técnicas e pelo alerta de estoque baixo).
     */
    @GetMapping("/busca")
    public PaginaResponse<MateriaPrimaResponse> buscar(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(defaultValue = "false") boolean estoqueBaixo,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho,
            @RequestParam(defaultValue = "nome") String ordenarPor,
            @RequestParam(defaultValue = "asc") String direcao) {
        return materiaPrimaService.buscarPaginado(busca, categoriaId, estoqueBaixo, pagina, tamanho, ordenarPor, direcao);
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
    public MateriaPrimaResponse atualizar(@PathVariable Long id, @Valid @RequestBody MateriaPrimaAtualizacaoRequest request) {
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
