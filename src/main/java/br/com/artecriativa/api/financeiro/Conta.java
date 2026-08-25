package br.com.artecriativa.api.financeiro;

import br.com.artecriativa.api.empresa.EntidadeComEmpresa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Conta a pagar ou a receber, com vencimento e status. O status ATRASADO não fica
 * armazenado de forma ingênua: é calculado em {@link #getStatusEfetivo()} sempre que uma
 * conta PENDENTE já passou do vencimento, sem depender de um job agendado.
 * <p>
 * Uma conta parcelada não é uma entidade à parte: {@link ContaService#criarParcelada}
 * gera N linhas de {@code Conta} independentes (cada parcela paga/edita/exclui sozinha,
 * do jeito que já funcionava antes) que só compartilham {@link #grupoParcelamentoId}
 * pra saber que vieram da mesma compra.
 */
@Entity
@Table(name = "contas")
@Getter
@Setter
@NoArgsConstructor
public class Conta extends EntidadeComEmpresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoConta tipo;

    @Column(nullable = false, length = 255)
    private String descricao;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    /** Custos extras de uma compra vinculada a esta conta (ver
     * {@code ContaRequest#itensMateriaPrima}) que não são de nenhuma matéria-prima
     * específica — ex: frete, taxas. Não gera movimentação de estoque nenhuma, só entra
     * na soma que precisa bater com {@link #valor} ({@code ContaService#validarItens}).
     * Default 0 pra qualquer conta sem itens vinculados. */
    @Column(name = "custos_extras", nullable = false, precision = 12, scale = 2)
    private BigDecimal custosExtras = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDate vencimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusConta status = StatusConta.PENDENTE;

    @Column(name = "pago_em")
    private Instant pagoEm;

    /** Nulos nos 3 abaixo = conta avulsa (não veio de um parcelamento). */
    @Column(name = "grupo_parcelamento_id")
    private UUID grupoParcelamentoId;

    @Column(name = "numero_parcela")
    private Integer numeroParcela;

    @Column(name = "total_parcelas")
    private Integer totalParcelas;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @PrePersist
    void aoPersistir() {
        criadoEm = Instant.now();
    }

    @Transient
    public StatusConta getStatusEfetivo() {
        if (status == StatusConta.PENDENTE && vencimento.isBefore(LocalDate.now())) {
            return StatusConta.ATRASADO;
        }
        return status;
    }
}
