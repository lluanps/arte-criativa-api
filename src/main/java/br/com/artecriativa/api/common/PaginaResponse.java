package br.com.artecriativa.api.common;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Envelope simples de paginação pra endpoints de busca — bem mais enxuto que o
 * {@link Page} puro do Spring (que serializa uma dúzia de campos, boa parte irrelevante
 * pro front). Usado só nos endpoints de busca paginada (ex: {@code GET /produtos/busca});
 * os endpoints de listagem simples (ex: {@code GET /produtos}) continuam devolvendo
 * {@code List<T>} direto — não dá pra trocar sem quebrar quem usa a lista inteira hoje
 * (seletores de produto/matéria-prima em Vendas, Fichas técnicas, Tutoriais, o alerta de
 * estoque baixo etc.).
 */
public record PaginaResponse<T>(
        List<T> conteudo,
        int pagina,
        int tamanho,
        long totalElementos,
        int totalPaginas
) {
    public static <E, T> PaginaResponse<T> de(Page<E> page, Function<E, T> mapeador) {
        return new PaginaResponse<>(
                page.getContent().stream().map(mapeador).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
