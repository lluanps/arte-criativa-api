package br.com.artecriativa.api.producao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReceitaRepository extends JpaRepository<Receita, Long> {
    Optional<Receita> findByProdutoId(Long produtoId);
}
