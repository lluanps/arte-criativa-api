package br.com.artecriativa.api.ideias;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IdeiaRepository extends JpaRepository<Ideia, Long> {

    // @EntityGraph pra trazer tags/fotos/produtoRelacionado na mesma query — mesmo
    // padrão de ReceitaRepository/TutorialRepository, por causa de open-in-view=false.
    @EntityGraph(attributePaths = {"tags", "fotosUrls", "produtoRelacionado"})
    List<Ideia> findAllByOrderByFavoritaDescAtualizadoEmDesc();

    @Override
    @EntityGraph(attributePaths = {"tags", "fotosUrls", "produtoRelacionado"})
    Optional<Ideia> findById(Long id);

    // Usado pra desvincular (não bloquear) quando o produto relacionado é excluído.
    List<Ideia> findByProdutoRelacionadoId(Long produtoId);
}
