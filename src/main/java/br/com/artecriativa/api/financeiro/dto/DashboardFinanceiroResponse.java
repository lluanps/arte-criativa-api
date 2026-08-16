package br.com.artecriativa.api.financeiro.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DashboardFinanceiroResponse(
        LocalDate periodoInicio,
        LocalDate periodoFim,
        BigDecimal totalReceitas,
        BigDecimal totalDespesas,
        BigDecimal saldo,
        BigDecimal totalContasPagarPendentes,
        BigDecimal totalContasReceberPendentes,
        long contasAtrasadas
) {
}
