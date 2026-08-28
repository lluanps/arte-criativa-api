package br.com.artecriativa.api.producao;

import br.com.artecriativa.api.empresa.EntidadeComEmpresa;
import br.com.artecriativa.api.estoque.Produto;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Ficha técnica de um produto: quais matérias-primas (e em que quantidade) são
 * consumidas para produzir {@code rendimento} unidades do produto.
 */
@Entity
@Table(name = "receitas")
@Getter
@Setter
@NoArgsConstructor
public class Receita extends EntidadeComEmpresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "produto_id", nullable = false, unique = true)
    private Produto produto;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal rendimento = BigDecimal.ONE;

    /** Custo de mão de obra por unidade produzida (não por lote/rendimento) — opcional,
     * default 0. Somado ao custo de insumo pra formar o custo real da ficha técnica; ver
     * {@code ReceitaResponse.custoTotal}. */
    @Column(name = "custo_mao_de_obra", nullable = false, precision = 12, scale = 2)
    private BigDecimal custoMaoDeObra = BigDecimal.ZERO;

    /** Embalagem, etiqueta, energia etc. por unidade produzida — mesma ideia do custo de
     * mão de obra acima. */
    @Column(name = "custo_embalagem_outros", nullable = false, precision = 12, scale = 2)
    private BigDecimal custoEmbalagemOutros = BigDecimal.ZERO;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @OneToMany(mappedBy = "receita", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<ReceitaItem> itens = new ArrayList<>();

    @PrePersist
    void aoPersistir() {
        criadoEm = Instant.now();
    }

    public void substituirItens(List<ReceitaItem> novosItens) {
        itens.clear();
        for (ReceitaItem item : novosItens) {
            item.setReceita(this);
            itens.add(item);
        }
    }
}
