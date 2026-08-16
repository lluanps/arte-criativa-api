package br.com.artecriativa.api.financeiro;

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

/**
 * Conta a pagar ou a receber, com vencimento e status. O status ATRASADO não fica
 * armazenado de forma ingênua: é calculado em {@link #getStatusEfetivo()} sempre que uma
 * conta PENDENTE já passou do vencimento, sem depender de um job agendado.
 */
@Entity
@Table(name = "contas")
@Getter
@Setter
@NoArgsConstructor
public class Conta {

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

    @Column(nullable = false)
    private LocalDate vencimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusConta status = StatusConta.PENDENTE;

    @Column(name = "pago_em")
    private Instant pagoEm;

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
