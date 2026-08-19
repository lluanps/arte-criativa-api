package br.com.artecriativa.api.vendas;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VendaRepository extends JpaRepository<Venda, Long> {

    // Sobrescritas com @EntityGraph pra trazer `itens` (e itens.produto, que o
    // VendaResponse navega) na mesma query. Necessário porque open-in-view é false —
    // o response é montado no controller, fora da transação do service, e um
    // fetchgraph implicitamente vira LAZY toda associação que não é listada aqui,
    // mesmo as @ManyToOne que por padrão são EAGER.
    @EntityGraph(attributePaths = {"itens", "itens.produto", "cliente", "canal"})
    List<Venda> findAllByOrderByDataVendaDesc();

    @Override
    @EntityGraph(attributePaths = {"itens", "itens.produto", "cliente", "canal"})
    Optional<Venda> findById(Long id);

    @EntityGraph(attributePaths = {"itens", "itens.produto", "cliente", "canal"})
    List<Venda> findByClienteIdOrderByDataVendaDesc(Long clienteId);

    // Usado pra decidir se um produto pode ser excluído em cascata (cadastro por
    // engano) ou só desativado — excluir um produto que já teve venda de verdade
    // apagaria histórico de faturamento. Também serve pra descrever quantas vendas
    // estão vinculadas quando a exclusão simples é bloqueada.
    long countByItens_ProdutoId(Long produtoId);

    // Idem, pra decidir se cliente/canal podem ser excluídos.
    long countByClienteId(Long clienteId);

    long countByCanalId(Long canalId);
}
