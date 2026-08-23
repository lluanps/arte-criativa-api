package br.com.artecriativa.api.estoque;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    long countByCategoriaId(Long categoriaId);

    // @EntityGraph pra trazer fotosUrls (agora @ElementCollection) na mesma query —
    // mesmo padrão de ReceitaRepository/TutorialRepository/IdeiaRepository, por causa
    // de open-in-view=false. IMPORTANTE: como isso vira um fetch graph custom, TODA
    // associação não listada vira LAZY, mesmo `categoria` (@ManyToOne, EAGER por
    // padrão) — por isso ela também entra na lista, senão quebra categoriaNome no
    // ProdutoResponse (ver memória de projeto sobre esse gotcha).
    @Override
    @EntityGraph(attributePaths = {"fotosUrls", "categoria"})
    List<Produto> findAll();

    @Override
    @EntityGraph(attributePaths = {"fotosUrls", "categoria"})
    Optional<Produto> findById(Long id);

    /**
     * Busca paginada com filtros opcionais — usada só pelo {@code GET /produtos/busca}
     * (listagem com busca/filtro/paginação de verdade); {@code findAll()} acima continua
     * existindo do jeito que está, pros vários lugares que precisam da lista inteira
     * (seletores, alerta de estoque baixo etc.).
     * <p>
     * Cada filtro "vazio" vira "não filtra por isso" — o padrão
     * {@code (:param IS NULL OR campo = :param)} é resolvido pela álgebra booleana do SQL
     * (TRUE OR qualquer-coisa = TRUE), não precisa de short-circuit. IMPORTANTE:
     * {@code busca} nunca deve chegar como {@code null} aqui — passe {@code ""} nesse
     * caso (ver {@link ProdutoService#buscarPaginado}). Um parâmetro null dentro de
     * {@code CONCAT(...)} faz o driver JDBC não conseguir inferir o tipo e o Postgres
     * assume bytea por padrão, quebrando o LOWER() com "function lower(bytea) does not
     * exist" — comparar com string vazia evita isso de vez.
     * <p>
     * Nota: aqui o {@code @EntityGraph} traz só {@code categoria} (não {@code fotosUrls}
     * como em {@code findAll()}/{@code findById()} acima) — {@code fotosUrls} é uma
     * {@code @ElementCollection}, e um fetch join de coleção nessa query faria o Hibernate
     * desistir do LIMIT/OFFSET no SQL e paginar "em memória" (busca tudo, corta depois em
     * Java), o que anula o ganho de paginar de verdade. {@code fotosUrls} continua vindo
     * certinho na resposta porque {@link ProdutoService#buscarPaginado} é
     * {@code @Transactional} — a sessão ainda tá aberta na hora de montar o
     * {@code ProdutoResponse}, então o lazy-load de cada foto acontece ali (mais uma
     * query por produto da página, não por produto do banco inteiro).
     */
    @EntityGraph(attributePaths = {"categoria"})
    @Query("""
            SELECT p FROM Produto p
            WHERE (:busca = '' OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :busca, '%')))
              AND (:categoriaId IS NULL OR p.categoria.id = :categoriaId)
              AND (:ativo IS NULL OR p.ativo = :ativo)
              AND (:estoqueBaixo = FALSE OR p.estoqueAtual <= p.estoqueMinimo)
            """)
    Page<Produto> buscar(@Param("busca") String busca,
                          @Param("categoriaId") Long categoriaId,
                          @Param("ativo") Boolean ativo,
                          @Param("estoqueBaixo") boolean estoqueBaixo,
                          Pageable pageable);
}
