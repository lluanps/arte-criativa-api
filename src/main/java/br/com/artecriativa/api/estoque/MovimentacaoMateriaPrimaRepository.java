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
}
