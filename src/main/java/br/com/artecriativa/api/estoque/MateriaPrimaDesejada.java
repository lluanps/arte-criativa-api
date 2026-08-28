package br.com.artecriativa.api.estoque;

import br.com.artecriativa.api.empresa.EntidadeComEmpresa;
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
 * "Lista de compras": uma matéria-prima que o usuário já sabe que vai comprar, mas
 * ainda não sabe o preço — só o nome, sem nenhuma relação com {@link MateriaPrima} até
 * a compra ser registrada de verdade (ver {@code MateriaPrimaService.criar}, que
 * recebe o nome de uma dessas e o usuário apaga esta manualmente depois).
 * <p>
 * Fica numa tabela totalmente separada de propósito: assim nunca corre risco de
 * aparecer no seletor de ficha técnica, no alerta de estoque baixo ou em qualquer
 * busca de matéria-prima "de verdade" — ela simplesmente não é uma linha em
 * {@code materias_primas} ainda.
 */
@Entity
@Table(name = "materias_primas_desejadas")
@Getter
@Setter
@NoArgsConstructor
public class MateriaPrimaDesejada extends EntidadeComEmpresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @PrePersist
    void aoPersistir() {
        criadoEm = Instant.now();
    }
}
