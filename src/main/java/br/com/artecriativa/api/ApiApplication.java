package br.com.artecriativa.api;

import br.com.artecriativa.api.empresa.TenantAwareRepositoryImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * {@code repositoryBaseClass = TenantAwareRepositoryImpl.class} reforça isolamento de
 * tenant em {@code findById}/{@code existsById}/{@code deleteById} pra todo repository do
 * projeto — é um no-op seguro pra entidade que não é multi-tenant, ver
 * {@link TenantAwareRepositoryImpl}.
 */
@SpringBootApplication
@EnableJpaRepositories(repositoryBaseClass = TenantAwareRepositoryImpl.class)
public class ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }
}
