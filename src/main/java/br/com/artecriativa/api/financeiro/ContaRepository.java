package br.com.artecriativa.api.financeiro;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ContaRepository extends JpaRepository<Conta, Long> {
    List<Conta> findByTipoOrderByVencimento(TipoConta tipo);

    List<Conta> findAllByOrderByVencimento();

    long countByGrupoParcelamentoId(UUID grupoParcelamentoId);
}
