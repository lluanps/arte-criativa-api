package br.com.artecriativa.api.estoque;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MovimentacaoMateriaPrimaRepository extends JpaRepository<MovimentacaoMateriaPrima, Long> {
    List<MovimentacaoMateriaPrima> findByMateriaPrimaIdOrderByDataMovimentacaoDesc(Long materiaPrimaId);

    long countByMateriaPrimaId(Long materiaPrimaId);

    List<MovimentacaoMateriaPrima> findByContaId(Long contaId);

    List<MovimentacaoMateriaPrima> findByGrupoParcelamentoId(UUID grupoParcelamentoId);

    boolean existsByContaId(Long contaId);

    boolean existsByGrupoParcelamentoId(UUID grupoParcelamentoId);

    /** Usado no estorno de uma compra vinculada a conta (ver
     * {@code MateriaPrimaService#estornarMovimentacoes}) — se existe alguma
     * movimentação MAIS NOVA que {@code id} pra essa matéria-prima, o custo médio
     * ponderado não pode ser revertido com segurança (a média já foi "misturada" com
     * o que aconteceu depois). */
    boolean existsByMateriaPrimaIdAndIdGreaterThan(Long materiaPrimaId, Long id);
}
