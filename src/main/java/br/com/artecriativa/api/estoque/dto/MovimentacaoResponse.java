package br.com.artecriativa.api.estoque.dto;

import br.com.artecriativa.api.estoque.MovimentacaoMateriaPrima;
import br.com.artecriativa.api.estoque.MovimentacaoProduto;
import br.com.artecriativa.api.estoque.TipoMovimentacao;

import java.math.BigDecimal;
import java.time.Instant;

public record MovimentacaoResponse(
        Long id,
        TipoMovimentacao tipo,
        String motivo,
        BigDecimal quantidade,
        String observacao,
        Instant dataMovimentacao,
        /** Só preenchido em movimentação de matéria-prima com valor pago informado
         * (ver {@code MovimentacaoMateriaPrimaRequest.valorPago}) — null em produto. */
        BigDecimal valorPago,
        BigDecimal custoUnitarioApurado
) {
    public static MovimentacaoResponse de(MovimentacaoProduto movimentacao) {
        return new MovimentacaoResponse(
                movimentacao.getId(),
                movimentacao.getTipo(),
                movimentacao.getMotivo().name(),
                movimentacao.getQuantidade(),
                movimentacao.getObservacao(),
                movimentacao.getDataMovimentacao(),
                null,
                null
        );
    }

    public static MovimentacaoResponse de(MovimentacaoMateriaPrima movimentacao) {
        return new MovimentacaoResponse(
                movimentacao.getId(),
                movimentacao.getTipo(),
                movimentacao.getMotivo().name(),
                movimentacao.getQuantidade(),
                movimentacao.getObservacao(),
                movimentacao.getDataMovimentacao(),
                movimentacao.getValorPago(),
                movimentacao.getCustoUnitarioApurado()
        );
    }
}
