package br.com.artecriativa.api.estoque;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimentacaoMateriaPrimaRepository extends JpaRepository<MovimentacaoMateriaPrima, Long> {
    List<MovimentacaoMateriaPrima> findByMateriaPrimaIdOrderByDataMovimentacaoDesc(Long materiaPrimaId);
}
