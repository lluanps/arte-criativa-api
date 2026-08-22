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
@Table(name = "movimentacoes_materia_prima")
@Getter
@Setter
@NoArgsConstructor
public class MovimentacaoMateriaPrima {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "materia_prima_id", nullable = false)
    private MateriaPrima materiaPrima;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMovimentacao tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MotivoMovimentacaoMateriaPrima motivo;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantidade;

    /** Quanto foi pago no total nesta entrada (ex: comprei 3kg de cera por R$ 100) --
     * opcional, só faz sentido em ENTRADA/COMPRA. Quando informado, o service calcula
     * {@code custoUnitarioApurado} e atualiza o custo unitário da matéria-prima. */
    @Column(name = "valor_pago", precision = 12, scale = 2)
    private BigDecimal valorPago;

    /** custo unitário desta compra especificamente (valorPago ÷ quantidade), guardado
     * pra dar um histórico de preço pago ao longo do tempo — não é necessariamente igual
     * ao custo unitário médio que fica em {@code MateriaPrima.custoUnitario} depois. */
    @Column(name = "custo_unitario_apurado", precision = 12, scale = 4)
    private BigDecimal custoUnitarioApurado;

    @Column(length = 500)
    private String observacao;

    @Column(name = "data_movimentacao", nullable = false)
    private Instant dataMovimentacao;

    @PrePersist
    void aoPersistir() {
        dataMovimentacao = Instant.now();
    }
}
