package br.com.artecriativa.api.cadastros;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaMateriaPrimaRepository extends JpaRepository<CategoriaMateriaPrima, Long> {

    List<CategoriaMateriaPrima> findAllByOrderByNome();

    boolean existsByNomeIgnoreCase(String nome);
}
