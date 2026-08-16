package br.com.artecriativa.api.vendas;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
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
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_nome", length = 150)
    private String clienteNome;

    @Column(length = 50)
    private String canal;

    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Column(name = "data_venda", nullable = false)
    private Instant dataVenda;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

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
}
