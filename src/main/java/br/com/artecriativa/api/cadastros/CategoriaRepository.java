package br.com.artecriativa.api.cadastros;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findAllByOrderByNome();

    boolean existsByNomeIgnoreCase(String nome);
}
