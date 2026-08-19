package br.com.artecriativa.api.producao;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReceitaRepository extends JpaRepository<Receita, Long> {

    // Sobrescritas com @EntityGraph pra trazer `itens` (e as associações que o
    // ReceitaResponse navega: produto, produto.categoria e itens.materiaPrima) na mesma
    // query. Necessário porque open-in-view é false — o response é montado no
    // controller, fora da transação do service, e um fetchgraph implicitamente vira
    // LAZY toda associação que não é listada aqui, mesmo as @ManyToOne/@OneToOne que
    // por padrão são EAGER.

    @Override
    @EntityGraph(attributePaths = {"itens", "itens.materiaPrima", "produto", "produto.categoria"})
    List<Receita> findAll();

    @Override
    @EntityGraph(attributePaths = {"itens", "itens.materiaPrima", "produto", "produto.categoria"})
    Optional<Receita> findById(Long id);

    @EntityGraph(attributePaths = {"itens", "itens.materiaPrima", "produto", "produto.categoria"})
    Optional<Receita> findByProdutoId(Long produtoId);

    // Usado pra saber se uma matéria-prima está usada em alguma ficha técnica, na hora
    // de decidir se ela pode ser excluída.
    long countByItens_MateriaPrimaId(Long materiaPrimaId);
}
