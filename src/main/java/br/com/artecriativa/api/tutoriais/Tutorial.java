package br.com.artecriativa.api.tutoriais;

import br.com.artecriativa.api.estoque.Produto;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Conteúdo passo a passo (texto + mídia), opcionalmente ligado a um produto — ex: o
 * tutorial de como fazer a "Vela Lavanda" referencia esse produto.
 */
@Entity
@Table(name = "tutoriais")
@Getter
@Setter
@NoArgsConstructor
public class Tutorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(length = 100)
    private String categoria;

    @ManyToOne
    @JoinColumn(name = "produto_relacionado_id")
    private Produto produtoRelacionado;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    @OneToMany(mappedBy = "tutorial", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem")
    private List<TutorialPasso> passos = new ArrayList<>();

    @PrePersist
    void aoPersistir() {
        criadoEm = Instant.now();
        atualizadoEm = Instant.now();
    }

    @PreUpdate
    void aoAtualizar() {
        atualizadoEm = Instant.now();
    }

    public void substituirPassos(List<TutorialPasso> novosPassos) {
        passos.clear();
        for (TutorialPasso passo : novosPassos) {
            passo.setTutorial(this);
            passos.add(passo);
        }
    }
}
