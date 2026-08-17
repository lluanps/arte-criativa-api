package br.com.artecriativa.api.cadastros;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CanalVendaRepository extends JpaRepository<CanalVenda, Long> {

    List<CanalVenda> findAllByOrderByNome();

    boolean existsByNomeIgnoreCase(String nome);
}
