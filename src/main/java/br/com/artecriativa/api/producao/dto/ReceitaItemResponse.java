package br.com.artecriativa.api.producao.dto;

import br.com.artecriativa.api.estoque.MateriaPrima;
import br.com.artecriativa.api.estoque.UnidadeMedida;
import br.com.artecriativa.api.producao.ReceitaItem;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record ReceitaItemResponse(
        Long id,
        Long materiaPrimaId,
        String materiaPrimaNome,
        String unidadeMedida,
        BigDecimal quantidade,
        /** Custo unitário da matéria-prima, na unidade cadastrada nela (não na unidade
         * deste item, que pode ser diferente) — ajuda a auditar de onde veio o subtotal. */
        BigDecimal custoUnitarioMateriaPrima,
        String unidadeMedidaMateriaPrima,
        /** quantidade (convertida pra unidade da matéria-prima) × custoUnitarioMateriaPrima. */
        BigDecimal subtotalCusto
) {
    public static ReceitaItemResponse de(ReceitaItem item) {
        MateriaPrima materiaPrima = item.getMateriaPrima();
        BigDecimal quantidadeNaUnidadeDaMateriaPrima = UnidadeMedida.converter(
                item.getQuantidade(), item.getUnidadeMedida(), materiaPrima.getUnidadeMedida());
        BigDecimal subtotal = quantidadeNaUnidadeDaMateriaPrima
                .multiply(materiaPrima.getCustoUnitario())
                .setScale(2, RoundingMode.HALF_UP);

        return new ReceitaItemResponse(
                item.getId(),
                materiaPrima.getId(),
                materiaPrima.getNome(),
                item.getUnidadeMedida(),
                item.getQuantidade(),
                materiaPrima.getCustoUnitario(),
                materiaPrima.getUnidadeMedida(),
                subtotal
        );
    }
}
