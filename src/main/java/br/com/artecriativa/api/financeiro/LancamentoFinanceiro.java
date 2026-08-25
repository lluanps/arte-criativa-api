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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Lançamento de receita ou despesa. Pode ser criado manualmente ou automaticamente por
 * outro módulo (ex: {@code Venda}), caso em que {@code origem}/{@code origemId} apontam
 * pro registro que o gerou. CRUD completo e dashboard ficam a cargo do módulo Financeiro
 * (Fase 4); por enquanto essa entidade só é escrita a partir de outros módulos.
 */
@Entity
@Table(name = "lancamentos_financeiros")
@Getter
@Setter
@NoArgsConstructor
public class LancamentoFinanceiro extends EntidadeComEmpresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoLancamento tipo;

    @Column(nullable = false, length = 100)
    private String categoria;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(length = 500)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrigemLancamento origem = OrigemLancamento.MANUAL;

    @Column(name = "origem_id")
    private Long origemId;

    @Column(name = "data_lancamento", nullable = false)
    private LocalDate dataLancamento;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @PrePersist
    void aoPersistir() {
        criadoEm = Instant.now();
        if (dataLancamento == null) {
            dataLancamento = LocalDate.now();
        }
    }
}
