package br.com.artecriativa.api.vendas.dto;

import br.com.artecriativa.api.vendas.Venda;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record VendaResponse(
        Long id,
        Long clienteId,
        String clienteNome,
        Long canalId,
        String canalNome,
        BigDecimal valorTotal,
        List<VendaItemResponse> itens,
        Instant dataVenda,
        Instant criadoEm
) {
    public static VendaResponse de(Venda venda) {
        return new VendaResponse(
                venda.getId(),
                venda.getCliente() != null ? venda.getCliente().getId() : null,
                venda.getCliente() != null ? venda.getCliente().getNome() : null,
                venda.getCanal() != null ? venda.getCanal().getId() : null,
                venda.getCanal() != null ? venda.getCanal().getNome() : null,
                venda.getValorTotal(),
                venda.getItens().stream().map(VendaItemResponse::de).toList(),
                venda.getDataVenda(),
                venda.getCriadoEm()
        );
    }
}
