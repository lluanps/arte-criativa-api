package br.com.artecriativa.api.cadastros;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Categoria de produto (ex: velas, xícaras). Cadastro simples — só o nome — usado
 * como FK em {@link br.com.artecriativa.api.estoque.Produto} no lugar de texto livre.
 */
@Entity
@Table(name = "categorias")
@Getter
@Setter
@NoArgsConstructor
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String nome;

    /** Faixa de preço de mercado pra produtos parecidos, preenchida manualmente via
     * pesquisa periódica (não é busca automática) — ver {@code CategoriaService}. */
    @Column(name = "preco_mercado_min", precision = 12, scale = 2)
    private BigDecimal precoMercadoMin;

    @Column(name = "preco_mercado_max", precision = 12, scale = 2)
    private BigDecimal precoMercadoMax;

    @Column(name = "preco_mercado_atualizado_em")
    private Instant precoMercadoAtualizadoEm;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @PrePersist
    void aoPersistir() {
        criadoEm = Instant.now();
    }
}
