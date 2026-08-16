package br.com.artecriativa.api.financeiro;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LancamentoFinanceiroRepository extends JpaRepository<LancamentoFinanceiro, Long> {
    List<LancamentoFinanceiro> findAllByOrderByDataLancamentoDesc();

    List<LancamentoFinanceiro> findByDataLancamentoBetweenOrderByDataLancamentoDesc(LocalDate inicio, LocalDate fim);
}
