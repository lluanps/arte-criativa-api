package br.com.artecriativa.api.estoque;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimentacaoProdutoRepository extends JpaRepository<MovimentacaoProduto, Long> {
    List<MovimentacaoProduto> findByProdutoIdOrderByDataMovimentacaoDesc(Long produtoId);
}
