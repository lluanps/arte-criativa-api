package br.com.artecriativa.api.estoque;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MateriaPrimaRepository extends JpaRepository<MateriaPrima, Long> {

    /**
     * Busca paginada com filtros opcionais — usada só pelo {@code GET /materias-primas/busca}
     * (listagem com busca/filtro/paginação de verdade); {@code findAll()} continua servindo
     * os vários lugares que precisam da lista inteira (seletor de matéria-prima em Fichas
     * técnicas, alerta de estoque baixo etc.).
     */
    // busca nunca deve chegar null aqui — passe "" (ver ProdutoRepository.buscar pro
    // porquê: parâmetro null dentro de CONCAT() quebra o LOWER() no Postgres).
    @Query("""
            SELECT mp FROM MateriaPrima mp
            WHERE (:busca = '' OR LOWER(mp.nome) LIKE LOWER(CONCAT('%', :busca, '%')))
              AND (:estoqueBaixo = FALSE OR mp.estoqueAtual <= mp.estoqueMinimo)
            """)
    Page<MateriaPrima> buscar(@Param("busca") String busca,
                               @Param("estoqueBaixo") boolean estoqueBaixo,
                               Pageable pageable);
}
