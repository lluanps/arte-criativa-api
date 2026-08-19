package br.com.artecriativa.api.estoque;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
