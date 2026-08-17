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

import java.time.Instant;

/**
 * Canal de venda (ex: Instagram, feira, loja física). Cadastro simples — só o nome —
 * usado como FK em {@link br.com.artecriativa.api.vendas.Venda} no lugar de texto livre.
 */
@Entity
@Table(name = "canais_venda")
@Getter
@Setter
@NoArgsConstructor
public class CanalVenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String nome;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @PrePersist
    void aoPersistir() {
        criadoEm = Instant.now();
    }
}
