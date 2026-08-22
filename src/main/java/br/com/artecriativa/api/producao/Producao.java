package br.com.artecriativa.api.producao;

import br.com.artecriativa.api.estoque.Produto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Registro de uma produção: baixa a matéria-prima consumida (via a {@link Receita} do
 * produto) e dá entrada no estoque do produto, com o custo total calculado a partir do
 * custo unitário das matérias-primas consumidas + mão de obra/embalagem da receita
 * (ambos opcionais, 0 quando a ficha técnica não os preenche).
 */
@Entity
@Table(name = "producoes")
@Getter
@Setter
@NoArgsConstructor
public class Producao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(name = "quantidade_produzida", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantidadeProduzida;

    @Column(name = "custo_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal custoTotal = BigDecimal.ZERO;

    @Column(length = 500)
    private String observacao;

    @Column(name = "data_producao", nullable = false)
    private Instant dataProducao;

    @PrePersist
    void aoPersistir() {
        dataProducao = Instant.now();
    }
}
