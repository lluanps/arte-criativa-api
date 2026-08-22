package br.com.artecriativa.api.producao;

import br.com.artecriativa.api.estoque.MateriaPrima;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Quantidade de uma matéria-prima necessária para produzir o rendimento de uma {@link Receita}.
 */
@Entity
@Table(name = "receita_itens")
@Getter
@Setter
@NoArgsConstructor
public class ReceitaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "receita_id", nullable = false)
    private Receita receita;

    @ManyToOne(optional = false)
    @JoinColumn(name = "materia_prima_id", nullable = false)
    private MateriaPrima materiaPrima;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantidade;

    /** Unidade de {@code quantidade} — texto livre, igual ao da matéria-prima (ver
     * {@code MateriaPrima.unidadeMedida}). Pode ser diferente da unidade cadastrada na
     * matéria-prima (ex: receita em "g" com a matéria-prima cadastrada em "kg"); nesse
     * caso o custo e o consumo de estoque são convertidos automaticamente — ver
     * {@link br.com.artecriativa.api.estoque.UnidadeMedida#converter}. */
    @Column(name = "unidade_medida", nullable = false, length = 20)
    private String unidadeMedida;
}
