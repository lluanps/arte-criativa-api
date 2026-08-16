package br.com.artecriativa.api.producao.dto;

import br.com.artecriativa.api.producao.ReceitaItem;

import java.math.BigDecimal;

public record ReceitaItemResponse(
        Long id,
        Long materiaPrimaId,
        String materiaPrimaNome,
        String unidadeMedida,
        BigDecimal quantidade
) {
    public static ReceitaItemResponse de(ReceitaItem item) {
        return new ReceitaItemResponse(
                item.getId(),
                item.getMateriaPrima().getId(),
                item.getMateriaPrima().getNome(),
                item.getMateriaPrima().getUnidadeMedida(),
                item.getQuantidade()
        );
    }
}
