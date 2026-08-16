package br.com.artecriativa.api.estoque;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

@Entity
@Table(name = "movimentacoes_produto")
@Getter
@Setter
@NoArgsConstructor
public class MovimentacaoProduto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMovimentacao tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MotivoMovimentacaoProduto motivo;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantidade;

    @Column(length = 500)
    private String observacao;

    @Column(name = "data_movimentacao", nullable = false)
    private Instant dataMovimentacao;

    @PrePersist
    void aoPersistir() {
        dataMovimentacao = Instant.now();
    }
}
