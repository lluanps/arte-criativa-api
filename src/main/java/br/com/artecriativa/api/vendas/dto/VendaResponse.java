package br.com.artecriativa.api.vendas.dto;

import br.com.artecriativa.api.vendas.Venda;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record VendaResponse(
        Long id,
        String clienteNome,
        String canal,
        BigDecimal valorTotal,
        List<VendaItemResponse> itens,
        Instant dataVenda,
        Instant criadoEm
) {
    public static VendaResponse de(Venda venda) {
        return new VendaResponse(
                venda.getId(),
                venda.getClienteNome(),
                venda.getCanal(),
                venda.getValorTotal(),
                venda.getItens().stream().map(VendaItemResponse::de).toList(),
                venda.getDataVenda(),
                venda.getCriadoEm()
        );
    }
}
