package br.com.artecriativa.api.empresa;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Diz ao Hibernate qual é o tenant (empresa) atual, lendo do {@link TenantContext} — que o
 * {@code JwtAuthFilter} popula a partir da claim {@code empresaId} do JWT. É usado pra
 * resolver o valor de todo campo {@code @TenantId} (ver {@link EntidadeComEmpresa}).
 * <p>
 * Não seta {@code hibernate.multiTenancy} — essa property é do mecanismo antigo
 * (schema/database por tenant, via {@code MultiTenantConnectionProvider}).
 * {@code @TenantId} é independente disso, só precisa deste resolver registrado.
 */
@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<Long>, HibernatePropertiesCustomizer {

    /**
     * Sentinel só pra evitar NPE se alguma query com {@code @TenantId} rodar sem
     * {@link TenantContext} setado — na prática não deveria acontecer, já que toda rota
     * exceto {@code /api/auth/**}/{@code /error} exige autenticação (ver SecurityConfig),
     * e nenhuma entidade tocada nessas rotas públicas tem {@code @TenantId}.
     */
    private static final Long SEM_TENANT = -1L;

    @Override
    public Long resolveCurrentTenantIdentifier() {
        Long empresaId = TenantContext.get();
        return empresaId != null ? empresaId : SEM_TENANT;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
    }
}
