package br.com.artecriativa.api.estoque;

import br.com.artecriativa.api.common.PaginaResponse;
import br.com.artecriativa.api.estoque.dto.MovimentacaoProdutoRequest;
import br.com.artecriativa.api.estoque.dto.MovimentacaoResponse;
import br.com.artecriativa.api.estoque.dto.ProdutoRequest;
import br.com.artecriativa.api.estoque.dto.ProdutoResponse;
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
@RequestMapping("/api/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService;

    @GetMapping
    public List<ProdutoResponse> listar() {
        return produtoService.listarTodos().stream().map(ProdutoResponse::de).toList();
    }

    /**
     * Busca paginada com filtros — usada pela tela de listagem de produtos. Diferente de
     * {@code listar()} acima (que devolve tudo, usado pelos seletores de produto em
     * Vendas/Fichas técnicas/Tutoriais e pelo alerta de estoque baixo).
     */
    @GetMapping("/busca")
    public PaginaResponse<ProdutoResponse> buscar(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(defaultValue = "todos") String status,
            @RequestParam(defaultValue = "false") boolean estoqueBaixo,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho,
            @RequestParam(defaultValue = "nome") String ordenarPor,
            @RequestParam(defaultValue = "asc") String direcao) {
        return produtoService.buscarPaginado(busca, categoriaId, status, estoqueBaixo, pagina, tamanho, ordenarPor, direcao);
    }

    @GetMapping("/{id}")
    public ProdutoResponse buscar(@PathVariable Long id) {
        return ProdutoResponse.de(produtoService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProdutoResponse criar(@Valid @RequestBody ProdutoRequest request) {
        return ProdutoResponse.de(produtoService.criar(request));
    }

    @PutMapping("/{id}")
    public ProdutoResponse atualizar(@PathVariable Long id, @Valid @RequestBody ProdutoRequest request) {
        return ProdutoResponse.de(produtoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        produtoService.excluir(id);
    }

    /**
     * Exclui o produto em cascata (movimentações, produção, ficha técnica) — usado
     * quando o cadastro foi por engano. Recusa (409) se o produto já teve venda.
     */
    @DeleteMapping("/{id}/definitivo")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirDefinitivamente(@PathVariable Long id) {
        produtoService.excluirDefinitivamente(id);
    }

    @PostMapping("/{id}/movimentacoes")
    @ResponseStatus(HttpStatus.CREATED)
    public MovimentacaoResponse registrarMovimentacao(@PathVariable Long id,
                                                        @Valid @RequestBody MovimentacaoProdutoRequest request) {
        return MovimentacaoResponse.de(produtoService.registrarMovimentacao(id, request));
    }

    @GetMapping("/{id}/movimentacoes")
    public List<MovimentacaoResponse> listarMovimentacoes(@PathVariable Long id) {
        return produtoService.listarMovimentacoes(id).stream().map(MovimentacaoResponse::de).toList();
    }
}
