package br.com.artecriativa.api.vendas.dto;

import br.com.artecriativa.api.vendas.VendaItem;

import java.math.BigDecimal;

public record VendaItemResponse(
        Long id,
        Long produtoId,
        String produtoNome,
        BigDecimal quantidade,
        BigDecimal precoUnitario,
        BigDecimal subtotal
) {
    public static VendaItemResponse de(VendaItem item) {
        return new VendaItemResponse(
                item.getId(),
                item.getProduto().getId(),
                item.getProduto().getNome(),
                item.getQuantidade(),
                item.getPrecoUnitario(),
                item.getSubtotal()
        );
    }
}
