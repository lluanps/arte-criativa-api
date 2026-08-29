package br.com.artecriativa.api.vendas.dto;

import br.com.artecriativa.api.vendas.StatusVenda;
import br.com.artecriativa.api.vendas.Venda;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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
        Instant criadoEm,
        LocalDate dataEntregaPrevista,
        StatusVenda status,
        BigDecimal valorSinal,
        BigDecimal valorSaldo,
        boolean entregaAtrasada
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
                venda.getCriadoEm(),
                venda.getDataEntregaPrevista(),
                venda.getStatus(),
                venda.getValorSinal(),
                venda.getValorSaldo(),
                venda.isEntregaAtrasada()
        );
    }
}
