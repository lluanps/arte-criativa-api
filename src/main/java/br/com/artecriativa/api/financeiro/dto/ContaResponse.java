package br.com.artecriativa.api.financeiro.dto;

import br.com.artecriativa.api.financeiro.Conta;
import br.com.artecriativa.api.financeiro.StatusConta;
import br.com.artecriativa.api.financeiro.TipoConta;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ContaResponse(
        Long id,
        TipoConta tipo,
        String descricao,
        BigDecimal valor,
        LocalDate vencimento,
        StatusConta status,
        Instant pagoEm,
        UUID grupoParcelamentoId,
        Integer numeroParcela,
        Integer totalParcelas,
        Instant criadoEm,
        /** Itens de matéria-prima vinculados (ver {@code ContaRequest#itensMateriaPrima})
         * — vazio pra qualquer conta comum, sem compra vinculada. */
        List<ItemMateriaPrimaCompraResponse> itensMateriaPrima,
        /** Ver {@code ContaRequest#custosExtras} — 0 pra qualquer conta sem itens vinculados. */
        BigDecimal custosExtras
) {
    public static ContaResponse de(Conta conta, List<ItemMateriaPrimaCompraResponse> itensMateriaPrima) {
        return new ContaResponse(
                conta.getId(),
                conta.getTipo(),
                conta.getDescricao(),
                conta.getValor(),
                conta.getVencimento(),
                conta.getStatusEfetivo(),
                conta.getPagoEm(),
                conta.getGrupoParcelamentoId(),
                conta.getNumeroParcela(),
                conta.getTotalParcelas(),
                conta.getCriadoEm(),
                itensMateriaPrima,
                conta.getCustosExtras()
        );
    }
}
