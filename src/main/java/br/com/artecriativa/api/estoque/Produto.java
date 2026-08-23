package br.com.artecriativa.api.estoque;

import br.com.artecriativa.api.cadastros.Categoria;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Produto final vendido (ex: vela, xícara). O estoque é mantido de forma
 * desnormalizada em {@code estoqueAtual} e atualizado a cada movimentação.
 */
@Entity
@Table(name = "produtos")
@Getter
@Setter
@NoArgsConstructor
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Column(name = "volume_ml", precision = 10, scale = 2)
    private BigDecimal volumeMl;

    @Column(name = "preco_venda", nullable = false, precision = 12, scale = 2)
    private BigDecimal precoVenda;

    /** Margem alvo sobre o custo de produção, usada pra calcular o preço sugerido na
     * ficha técnica (ver {@code ReceitaResponse}). Nula = usa o padrão do sistema. */
    @Column(name = "margem_desejada_percentual", precision = 6, scale = 2)
    private BigDecimal margemDesejadaPercentual;

    @Column(name = "estoque_atual", nullable = false, precision = 12, scale = 3)
    private BigDecimal estoqueAtual = BigDecimal.ZERO;

    @Column(name = "estoque_minimo", nullable = false, precision = 12, scale = 3)
    private BigDecimal estoqueMinimo = BigDecimal.ZERO;

    /** Até 5 fotos (ver validação em {@code ProdutoRequest}) — a primeira funciona
     * como capa nas listagens. */
    @ElementCollection
    @CollectionTable(name = "produto_fotos", joinColumns = @JoinColumn(name = "produto_id"))
    @OrderColumn(name = "ordem")
    @Column(name = "url", length = 1000)
    private List<String> fotosUrls = new ArrayList<>();

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    /** Lock otimista: evita "lost update" quando duas requisições concorrentes
     * (ex: duas movimentações de estoque quase simultâneas) leem-calculam-salvam o
     * mesmo registro — o Hibernate compara essa versão em memória com a do banco no
     * flush e lança {@link org.springframework.orm.ObjectOptimisticLockingFailureException}
     * (traduzida pra 409 em {@code GlobalExceptionHandler}) se alguém já salvou por
     * baixo. Gerenciado inteiramente pelo JPA — nunca setar/ler na mão. */
    @Version
    @Column(name = "versao", nullable = false)
    private Long versao;

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
