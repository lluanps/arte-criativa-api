package br.com.artecriativa.api.estoque;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

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

    @Column(length = 100)
    private String categoria;

    @Column(name = "preco_venda", nullable = false, precision = 12, scale = 2)
    private BigDecimal precoVenda;

    @Column(name = "estoque_atual", nullable = false, precision = 12, scale = 3)
    private BigDecimal estoqueAtual = BigDecimal.ZERO;

    @Column(name = "estoque_minimo", nullable = false, precision = 12, scale = 3)
    private BigDecimal estoqueMinimo = BigDecimal.ZERO;

    @Column(name = "foto_url", length = 500)
    private String fotoUrl;

    @Column(nullable = false)
    private boolean ativo = true;

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
