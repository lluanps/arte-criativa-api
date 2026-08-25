package br.com.artecriativa.api.empresa;

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
 * O tenant em si — o negócio de cada cliente do sistema. Não estende
 * {@link EntidadeComEmpresa}: ela É a empresa, não pertence a uma.
 * <p>
 * Endereço fica como texto livre por ora (sem rua/cidade/UF/CEP separados) — só precisa
 * virar estruturado quando (e se) entrar emissão de nota fiscal, fase futura.
 */
@Entity
@Table(name = "empresas")
@Getter
@Setter
@NoArgsConstructor
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 150)
    private String email;

    @Column(length = 30)
    private String telefone;

    @Column(name = "cnpj_ou_cpf", length = 20)
    private String cnpjOuCpf;

    @Column(columnDefinition = "TEXT")
    private String endereco;

    @Column(name = "logotipo_url", length = 500)
    private String logotipoUrl;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @PrePersist
    void aoPersistir() {
        criadoEm = Instant.now();
    }
}
