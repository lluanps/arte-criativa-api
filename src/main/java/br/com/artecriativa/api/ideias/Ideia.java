package br.com.artecriativa.api.ideias;

import br.com.artecriativa.api.estoque.Produto;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
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
 * Anotação livre de inspiração/ideia de produto — o "caderno" do ateliê. Pode ter
 * fotos, tags e opcionalmente apontar pra um produto já cadastrado (ex: "ideia de
 * variação da vela X"). Esse vínculo é só informativo: excluir o produto nunca
 * bloqueia, apenas desvincula (ver {@code ProdutoService}).
 */
@Entity
@Table(name = "ideias")
@Getter
@Setter
@NoArgsConstructor
public class Ideia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(columnDefinition = "text")
    private String corpo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusIdeia status = StatusIdeia.IDEIA_SOLTA;

    @Column(nullable = false)
    private boolean favorita = false;

    @ManyToOne
    @JoinColumn(name = "produto_relacionado_id")
    private Produto produtoRelacionado;

    @ElementCollection
    @CollectionTable(name = "ideia_tags", joinColumns = @JoinColumn(name = "ideia_id"))
    @OrderColumn(name = "ordem")
    @Column(name = "tag", length = 50)
    private List<String> tags = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "ideia_fotos", joinColumns = @JoinColumn(name = "ideia_id"))
    @OrderColumn(name = "ordem")
    @Column(name = "url", length = 1000)
    private List<String> fotosUrls = new ArrayList<>();

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    @PrePersist
    void aoPersistir() {
        criadoEm = Instant.now();
        atualizadoEm = Instant.now();
    }

    @PreUpdate
    void aoAtualizar() {
        atualizadoEm = Instant.now();
    }
}
