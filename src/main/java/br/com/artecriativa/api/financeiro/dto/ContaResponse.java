package br.com.artecriativa.api.financeiro.dto;

import br.com.artecriativa.api.financeiro.Conta;
import br.com.artecriativa.api.financeiro.StatusConta;
import br.com.artecriativa.api.financeiro.TipoConta;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ContaResponse(
        Long id,
        TipoConta tipo,
        String descricao,
        BigDecimal valor,
        LocalDate vencimento,
        StatusConta status,
        Instant pagoEm,
        Instant criadoEm
) {
    public static ContaResponse de(Conta conta) {
        return new ContaResponse(
                conta.getId(),
                conta.getTipo(),
                conta.getDescricao(),
                conta.getValor(),
                conta.getVencimento(),
                conta.getStatusEfetivo(),
                conta.getPagoEm(),
                conta.getCriadoEm()
        );
    }
}
