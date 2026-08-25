package br.com.artecriativa.api.estoque;

import br.com.artecriativa.api.empresa.EntidadeComEmpresa;
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
import java.util.UUID;

@Entity
@Table(name = "movimentacoes_materia_prima")
@Getter
@Setter
@NoArgsConstructor
public class MovimentacaoMateriaPrima extends EntidadeComEmpresa {

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

    /** Preenchido quando esta entrada nasceu do registro de uma {@code Conta} avulsa a
     * pagar (compra de matéria-prima em si) — não é uma FK de verdade (mesmo padrão de
     * {@code LancamentoFinanceiro.origemId}/{@code Conta.grupoParcelamentoId}), só um
     * id de correlação validado na aplicação. Nunca preenchido junto com
     * {@link #grupoParcelamentoId}. */
    @Column(name = "conta_id")
    private Long contaId;

    /** Mesma ideia de {@link #contaId}, mas quando a conta de origem é parcelada — o
     * grupo inteiro, não uma parcela específica (ver {@code ContaService}). */
    @Column(name = "grupo_parcelamento_id")
    private UUID grupoParcelamentoId;

    @Column(name = "data_movimentacao", nullable = false)
    private Instant dataMovimentacao;

    @PrePersist
    void aoPersistir() {
        dataMovimentacao = Instant.now();
    }
}
