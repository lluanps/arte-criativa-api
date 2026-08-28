package br.com.artecriativa.api.empresa;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.TenantId;

/**
 * Superclasse mapeada de toda entidade de negócio isolada por empresa (tenant). O
 * Hibernate usa {@link TenantId} pra aplicar {@code WHERE empresa_id = ?} automaticamente
 * em {@code findAll}/{@code @Query} JPQL e preencher a coluna sozinho no INSERT, a partir
 * do {@link TenantIdentifierResolver}/{@link TenantContext} — nenhum service precisa setar
 * {@code empresaId} na mão ao criar algo.
 * <p>
 * De propósito sem setter público: {@code @TenantId} é preenchido pelo Hibernate, nunca
 * deve vir de um DTO — um setter convidaria alguém a tentar setar manualmente vindo de
 * requisição, que é exatamente o vetor de vazamento entre empresas que este mecanismo
 * existe pra evitar.
 * <p>
 * {@code findById}/{@code existsById}/{@code deleteById} têm uma limitação conhecida do
 * Hibernate (HHH-16830: {@code EntityManager.find()} pode não aplicar o filtro de
 * {@code @TenantId} em algumas versões) — por isso {@link TenantAwareRepositoryImpl}
 * confere o {@code empresaId} de novo, como defesa em profundidade, em vez de confiar só
 * no Hibernate pra esses três métodos.
 */
@MappedSuperclass
public abstract class EntidadeComEmpresa {

    @TenantId
    @Column(name = "empresa_id", nullable = false, updatable = false)
    private Long empresaId;

    public Long getEmpresaId() {
        return empresaId;
    }
}
