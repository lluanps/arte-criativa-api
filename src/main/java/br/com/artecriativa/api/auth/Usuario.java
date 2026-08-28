package br.com.artecriativa.api.auth;

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

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 150, unique = true)
    private String email;

    @Column(name = "senha_hash", nullable = false, length = 255)
    private String senhaHash;

    /**
     * De propósito uma coluna comum, NÃO {@code @TenantId} — a busca de login (por
     * e-mail, em {@code AuthService.login}) roda antes de saber a empresa, então não pode
     * ficar automaticamente filtrada por tenant (ver
     * {@link br.com.artecriativa.api.empresa.EntidadeComEmpresa}, que {@code Usuario} de
     * propósito não estende). Setado manualmente em {@code AuthService.registrar}, sempre
     * a partir do usuário autenticado que está chamando, nunca de um campo de request.
     */
    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @PrePersist
    void aoPersistir() {
        criadoEm = Instant.now();
    }
}
