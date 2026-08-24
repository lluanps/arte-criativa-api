package br.com.artecriativa.api.producao.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Quantas unidades do produto dá pra produzir com o estoque atual de uma matéria-prima,
 * dada a receita — usado por {@link ReceitaItemResponse} (por item, isoladamente) e por
 * {@link ReceitaResponse} (o mínimo entre todos os itens, que é o que de fato limita a
 * produção). Arredondado pra baixo: "dá pra produzir 4,7 unidades" não faz sentido, o
 * produto acabado é sempre uma quantidade inteira.
 */
final class UnidadesProduziveis {

    private UnidadesProduziveis() {
    }

    /**
     * {@code estoqueAtual × rendimento ÷ quantidadeConsumidaPorLote} — equivalente a
     * dividir o estoque pelo consumo por unidade ({@code quantidadeConsumidaPorLote ÷
     * rendimento}), só que calculado nessa ordem pra evitar arredondar o consumo por
     * unidade antes da divisão final (menos uma fonte de erro acumulado). {@code null}
     * se a receita não tiver como calcular (rendimento inválido) ou o item não consumir
     * nada de fato (quantidade zero — não deveria acontecer, itens exigem quantidade >
     * 0, mas defensivo contra dado antigo/corrompido).
     */
    static Long calcular(BigDecimal estoqueAtual, BigDecimal quantidadeConsumidaPorLote, BigDecimal rendimento) {
        if (rendimento == null || rendimento.compareTo(BigDecimal.ZERO) <= 0
                || quantidadeConsumidaPorLote == null || quantidadeConsumidaPorLote.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        if (estoqueAtual == null || estoqueAtual.compareTo(BigDecimal.ZERO) <= 0) {
            return 0L;
        }
        BigDecimal unidades = estoqueAtual.multiply(rendimento)
                .divide(quantidadeConsumidaPorLote, 0, RoundingMode.DOWN);
        return unidades.longValue();
    }
}
