package br.com.artecriativa.api.financeiro.dto;

import br.com.artecriativa.api.financeiro.LancamentoFinanceiro;
import br.com.artecriativa.api.financeiro.OrigemLancamento;
import br.com.artecriativa.api.financeiro.TipoLancamento;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record LancamentoFinanceiroResponse(
        Long id,
        TipoLancamento tipo,
        String categoria,
        BigDecimal valor,
        String descricao,
        OrigemLancamento origem,
        Long origemId,
        LocalDate dataLancamento,
        Instant criadoEm
) {
    public static LancamentoFinanceiroResponse de(LancamentoFinanceiro lancamento) {
        return new LancamentoFinanceiroResponse(
                lancamento.getId(),
                lancamento.getTipo(),
                lancamento.getCategoria(),
                lancamento.getValor(),
                lancamento.getDescricao(),
                lancamento.getOrigem(),
                lancamento.getOrigemId(),
                lancamento.getDataLancamento(),
                lancamento.getCriadoEm()
        );
    }
}
