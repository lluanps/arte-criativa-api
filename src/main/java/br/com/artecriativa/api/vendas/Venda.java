package br.com.artecriativa.api.vendas;

import br.com.artecriativa.api.cadastros.CanalVenda;
import br.com.artecriativa.api.cadastros.Cliente;
import br.com.artecriativa.api.empresa.EntidadeComEmpresa;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Pedido de venda: um ou mais {@link VendaItem} de produtos, com baixa automática do
 * estoque de cada produto vendido e lançamento financeiro de receita correspondente.
 */
@Entity
@Table(name = "vendas")
@Getter
@Setter
@NoArgsConstructor
public class Venda extends EntidadeComEmpresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "canal_id")
    private CanalVenda canal;

    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Column(name = "data_venda", nullable = false)
    private Instant dataVenda;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    /** Null = venda de balcão, imediata. Preenchida = encomenda. */
    @Column(name = "data_entrega_prevista")
    private LocalDate dataEntregaPrevista;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusVenda status = StatusVenda.ENTREGUE;

    @Column(name = "valor_sinal", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorSinal = BigDecimal.ZERO;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<VendaItem> itens = new ArrayList<>();

    @PrePersist
    void aoPersistir() {
        dataVenda = Instant.now();
        criadoEm = Instant.now();
    }

    public void adicionarItens(List<VendaItem> novosItens) {
        for (VendaItem item : novosItens) {
            item.setVenda(this);
            itens.add(item);
        }
    }

    @Transient
    public BigDecimal getValorSaldo() {
        return valorTotal.subtract(valorSinal);
    }

    /** Mesmo princípio de {@code Conta.getStatusEfetivo()}: calculado em leitura, sem
     * scheduler. Diferente de {@code Conta} (só PAGO/PENDENTE), aqui o atraso pode
     * ocorrer em qualquer estágio não-terminal — por isso é um transiente separado, não
     * um valor a mais no enum. */
    @Transient
    public boolean isEntregaAtrasada() {
        return dataEntregaPrevista != null
                && status != StatusVenda.ENTREGUE
                && dataEntregaPrevista.isBefore(LocalDate.now());
    }
}
