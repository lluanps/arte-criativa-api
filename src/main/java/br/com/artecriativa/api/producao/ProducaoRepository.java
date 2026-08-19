package br.com.artecriativa.api.producao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProducaoRepository extends JpaRepository<Producao, Long> {
    List<Producao> findByProdutoIdOrderByDataProducaoDesc(Long produtoId);

    long countByProdutoId(Long produtoId);

    void deleteByProdutoId(Long produtoId);
}
