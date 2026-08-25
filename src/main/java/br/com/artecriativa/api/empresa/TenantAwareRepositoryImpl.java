package br.com.artecriativa.api.empresa;

import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import java.util.Objects;
import java.util.Optional;

/**
 * Repository base class registrada globalmente (ver {@code @EnableJpaRepositories} em
 * {@code ApiApplication}) que reforça o isolamento por tenant em {@code findById}/
 * {@code existsById}/{@code deleteById} — os três métodos que usam
 * {@code EntityManager.find()} por baixo, onde o {@code @TenantId} do Hibernate tem uma
 * limitação conhecida (HHH-16830: pode não aplicar o filtro). Em vez de confiar só nisso,
 * ou de espalhar uma checagem em cada service, o isolamento fica garantido num lugar só.
 * <p>
 * É um no-op seguro pra entidade que não é multi-tenant (ex: {@code Usuario},
 * {@code Empresa}) — só filtra quem estende {@link EntidadeComEmpresa}.
 * <p>
 * {@code findAll()} e toda {@code @Query} JPQL continuam cobertos pelo {@code @TenantId}
 * do Hibernate normalmente (esses passam por geração de SQL de verdade, não pelo atalho de
 * {@code EntityManager.find()}) — não precisam de reforço aqui.
 */
public class TenantAwareRepositoryImpl<T, ID> extends SimpleJpaRepository<T, ID> {

    public TenantAwareRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
    }

    @Override
    public Optional<T> findById(ID id) {
        return super.findById(id).filter(this::pertenceAoTenantAtual);
    }

    @Override
    public boolean existsById(ID id) {
        return findById(id).isPresent();
    }

    @Override
    public void deleteById(ID id) {
        findById(id).ifPresent(super::delete);
    }

    private boolean pertenceAoTenantAtual(T entidade) {
        if (!(entidade instanceof EntidadeComEmpresa comEmpresa)) {
            return true;
        }
        return Objects.equals(comEmpresa.getEmpresaId(), TenantContext.get());
    }
}
