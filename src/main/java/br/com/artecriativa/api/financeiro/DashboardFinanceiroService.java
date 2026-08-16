package br.com.artecriativa.api.financeiro;

import br.com.artecriativa.api.financeiro.dto.DashboardFinanceiroResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/**
 * Resumo de fluxo de caixa: receitas x despesas lançadas num período (padrão: mês
 * corrente) e a situação das contas a pagar/receber (independente do período, já que
 * refletem um saldo em aberto, não um fluxo).
 */
@Service
@RequiredArgsConstructor
public class DashboardFinanceiroService {

    private final LancamentoFinanceiroRepository lancamentoFinanceiroRepository;
    private final ContaRepository contaRepository;

    @Transactional(readOnly = true)
    public DashboardFinanceiroResponse gerar(LocalDate inicio, LocalDate fim) {
        LocalDate periodoInicio = inicio != null ? inicio : LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        LocalDate periodoFim = fim != null ? fim : LocalDate.now();

        List<LancamentoFinanceiro> lancamentos = lancamentoFinanceiroRepository
                .findByDataLancamentoBetweenOrderByDataLancamentoDesc(periodoInicio, periodoFim);

        BigDecimal totalReceitas = somar(lancamentos, TipoLancamento.RECEITA);
        BigDecimal totalDespesas = somar(lancamentos, TipoLancamento.DESPESA);

        List<Conta> contas = contaRepository.findAll();

        BigDecimal totalContasPagarPendentes = contas.stream()
                .filter(c -> c.getTipo() == TipoConta.PAGAR && c.getStatusEfetivo() != StatusConta.PAGO)
                .map(Conta::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalContasReceberPendentes = contas.stream()
                .filter(c -> c.getTipo() == TipoConta.RECEBER && c.getStatusEfetivo() != StatusConta.PAGO)
                .map(Conta::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long contasAtrasadas = contas.stream()
                .filter(c -> c.getStatusEfetivo() == StatusConta.ATRASADO)
                .count();

        return new DashboardFinanceiroResponse(
                periodoInicio,
                periodoFim,
                totalReceitas,
                totalDespesas,
                totalReceitas.subtract(totalDespesas),
                totalContasPagarPendentes,
                totalContasReceberPendentes,
                contasAtrasadas
        );
    }

    private BigDecimal somar(List<LancamentoFinanceiro> lancamentos, TipoLancamento tipo) {
        return lancamentos.stream()
                .filter(l -> l.getTipo() == tipo)
                .map(LancamentoFinanceiro::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
