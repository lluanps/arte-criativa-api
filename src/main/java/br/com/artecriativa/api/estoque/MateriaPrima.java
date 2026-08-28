package br.com.artecriativa.api.estoque;

import br.com.artecriativa.api.cadastros.CategoriaMateriaPrima;
import br.com.artecriativa.api.cadastros.Fornecedor;
import br.com.artecriativa.api.empresa.EntidadeComEmpresa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Insumo consumido na produção (ex: cera, pavio, essência, argila).
 */
@Entity
@Table(name = "materias_primas")
@Getter
@Setter
@NoArgsConstructor
public class MateriaPrima extends EntidadeComEmpresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private CategoriaMateriaPrima categoria;

    /** Texto livre (ex: "g", "kg", "ml", "un") — ver {@link UnidadeMedida#deTexto} pra
     * conversão entre unidades reconhecidas quando a ficha técnica usa uma unidade
     * diferente da cadastrada aqui. */
    @Column(name = "unidade_medida", nullable = false, length = 20)
    private String unidadeMedida;

    @Column(name = "custo_unitario", nullable = false, precision = 12, scale = 4)
    private BigDecimal custoUnitario = BigDecimal.ZERO;

    @Column(name = "estoque_atual", nullable = false, precision = 12, scale = 3)
    private BigDecimal estoqueAtual = BigDecimal.ZERO;

    @Column(name = "estoque_minimo", nullable = false, precision = 12, scale = 3)
    private BigDecimal estoqueMinimo = BigDecimal.ZERO;

    /** Antes era texto livre (coluna {@code fornecedor}) — virou cadastro de verdade
     * (ver {@link Fornecedor}) pra poder consultar "o que compro de quem" e corrigir o
     * nome num lugar só sem quebrar o vínculo com o histórico. */
    @ManyToOne
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

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
