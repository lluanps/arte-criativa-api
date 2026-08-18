package br.com.artecriativa.api.financeiro;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LancamentoFinanceiroRepository extends JpaRepository<LancamentoFinanceiro, Long> {
    List<LancamentoFinanceiro> findAllByOrderByDataLancamentoDesc();

    List<LancamentoFinanceiro> findByDataLancamentoBetweenOrderByDataLancamentoDesc(LocalDate inicio, LocalDate fim);

    /** Usado por outros módulos (ex: Venda) pra achar/remover o lançamento que geraram —
     * {@code LancamentoFinanceiroService.excluir} recusa mexer em lançamentos não-MANUAL
     * diretamente, então essa remoção em cascata é feita pelo módulo dono da origem. */
    Optional<LancamentoFinanceiro> findByOrigemAndOrigemId(OrigemLancamento origem, Long origemId);
}
