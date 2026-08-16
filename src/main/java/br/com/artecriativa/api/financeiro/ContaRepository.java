package br.com.artecriativa.api.financeiro;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContaRepository extends JpaRepository<Conta, Long> {
    List<Conta> findByTipoOrderByVencimento(TipoConta tipo);

    List<Conta> findAllByOrderByVencimento();
}
