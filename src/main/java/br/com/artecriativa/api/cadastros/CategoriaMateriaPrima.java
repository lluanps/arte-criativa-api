package br.com.artecriativa.api.cadastros;

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
 * Categoria de matéria-prima (ex: ceras, pavios, embalagens, adesivos) — cadastro
 * simples, só o nome, usado como FK em {@link br.com.artecriativa.api.estoque.MateriaPrima}.
 * Separada de {@link Categoria} (categoria de PRODUTO) de propósito: são assuntos
 * diferentes — "Ceras"/"Pavios" não fazem sentido misturados com "Velas"/"Xícaras" no
 * mesmo dropdown, e Categoria carrega campos específicos de produto (preço de mercado)
 * que não fazem sentido pra um insumo.
 */
@Entity
@Table(name = "categorias_materia_prima")
@Getter
@Setter
@NoArgsConstructor
public class CategoriaMateriaPrima extends EntidadeComEmpresa {

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
