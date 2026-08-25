package br.com.artecriativa.api.empresa;

import br.com.artecriativa.api.cadastros.Fornecedor;
import br.com.artecriativa.api.cadastros.FornecedorRepository;
import br.com.artecriativa.api.financeiro.Conta;
import br.com.artecriativa.api.financeiro.ContaRepository;
import br.com.artecriativa.api.financeiro.TipoConta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Primeiro teste de integração real do projeto (até aqui só existia unit puro com
 * Mockito) — de propósito: isolamento de tenant só pode ser provado com Hibernate/Postgres
 * de verdade rodando, mockar o repository testaria o mock, não o mecanismo real
 * ({@code @TenantId} + {@link TenantAwareRepositoryImpl}).
 * <p>
 * Nome termina em {@code Test} (não {@code IT}) de propósito — o projeto não tem Failsafe
 * configurado, só {@code mvn test}/Surefire padrão, que só pega {@code *Test.java}.
 * <p>
 * Escopo do piloto: {@code Fornecedor} e {@code Conta} (uma entidade de cada módulo
 * sensível) — como o mecanismo é uniforme via {@link EntidadeComEmpresa}, provar pra essas
 * duas é suficiente pra confiar no rollout mecânico pras demais entidades.
 */
@SpringBootTest
@Testcontainers
class MultiTenancyIsolamentoTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private FornecedorRepository fornecedorRepository;
    @Autowired
    private ContaRepository contaRepository;

    private Long empresaA;
    private Long empresaB;

    @BeforeEach
    void criarEmpresas() {
        empresaA = empresaRepository.save(novaEmpresa("Empresa A")).getId();
        empresaB = empresaRepository.save(novaEmpresa("Empresa B")).getId();
    }

    @AfterEach
    void limparTenantContext() {
        TenantContext.clear();
    }

    @Test
    void listar_retornaSoDadosDaPropriaEmpresa() {
        TenantContext.set(empresaA);
        fornecedorRepository.save(novoFornecedor("Fornecedor de A"));

        TenantContext.set(empresaB);
        fornecedorRepository.save(novoFornecedor("Fornecedor de B"));

        TenantContext.set(empresaA);
        List<Fornecedor> vistosPorA = fornecedorRepository.findAll();

        assertThat(vistosPorA).extracting(Fornecedor::getNome).containsExactly("Fornecedor de A");
    }

    @Test
    void findById_deRecursoDeOutraEmpresa_naoVaza() {
        TenantContext.set(empresaA);
        Long idContaDeA = contaRepository.save(novaConta("Conta de A")).getId();

        TenantContext.set(empresaB);

        assertThat(contaRepository.findById(idContaDeA)).isEmpty();
    }

    @Test
    void salvar_herdaEmpresaIdAutomaticamenteSemSetarNaMao() {
        TenantContext.set(empresaA);

        Fornecedor salvo = fornecedorRepository.save(novoFornecedor("Fornecedor sem empresaId manual"));

        assertThat(salvo.getEmpresaId()).isEqualTo(empresaA);
    }

    private static Empresa novaEmpresa(String nome) {
        Empresa empresa = new Empresa();
        empresa.setNome(nome);
        return empresa;
    }

    private static Fornecedor novoFornecedor(String nome) {
        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setNome(nome);
        return fornecedor;
    }

    private static Conta novaConta(String descricao) {
        Conta conta = new Conta();
        conta.setTipo(TipoConta.PAGAR);
        conta.setDescricao(descricao);
        conta.setValor(BigDecimal.TEN);
        conta.setVencimento(LocalDate.now().plusDays(30));
        return conta;
    }
}
