package br.com.artecriativa.api.tutoriais;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {

    // @EntityGraph pra trazer `passos` e `produtoRelacionado` na mesma query — ver
    // memória de projeto sobre open-in-view=false + @OneToMany lazy (o mesmo padrão de
    // ReceitaRepository/VendaRepository).
    @Override
    @EntityGraph(attributePaths = {"passos", "produtoRelacionado"})
    List<Tutorial> findAll();

    @Override
    @EntityGraph(attributePaths = {"passos", "produtoRelacionado"})
    Optional<Tutorial> findById(Long id);

    @EntityGraph(attributePaths = {"passos", "produtoRelacionado"})
    List<Tutorial> findByProdutoRelacionadoId(Long produtoId);
}
