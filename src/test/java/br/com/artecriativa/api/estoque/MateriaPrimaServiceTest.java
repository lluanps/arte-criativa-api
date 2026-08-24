package br.com.artecriativa.api.estoque;

import br.com.artecriativa.api.cadastros.CategoriaMateriaPrimaRepository;
import br.com.artecriativa.api.common.ConflitoOperacaoException;
import br.com.artecriativa.api.estoque.dto.MateriaPrimaRequest;
import br.com.artecriativa.api.estoque.dto.MovimentacaoMateriaPrimaRequest;
import br.com.artecriativa.api.financeiro.LancamentoFinanceiro;
import br.com.artecriativa.api.financeiro.LancamentoFinanceiroRepository;
import br.com.artecriativa.api.financeiro.OrigemLancamento;
import br.com.artecriativa.api.financeiro.TipoLancamento;
import br.com.artecriativa.api.producao.ReceitaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Cobre a lógica financeira mais delicada de {@link MateriaPrimaService}: custo médio
 * ponderado, bloqueio de estoque negativo, exclusão vinculada e o estorno de compra
 * via {@code Conta} (reversão em ordem + os dois bloqueios de segurança contra "lost
 * update" no custo médio).
 */
@ExtendWith(MockitoExtension.class)
class MateriaPrimaServiceTest {

    @Mock
    private MateriaPrimaRepository materiaPrimaRepository;
    @Mock
    private MovimentacaoMateriaPrimaRepository movimentacaoRepository;
    @Mock
    private ReceitaRepository receitaRepository;
    @Mock
    private LancamentoFinanceiroRepository lancamentoFinanceiroRepository;
    @Mock
    private CategoriaMateriaPrimaRepository categoriaRepository;

    @InjectMocks
    private MateriaPrimaService service;

    private static MateriaPrima materiaPrimaExistente(BigDecimal estoque, BigDecimal custo) {
        MateriaPrima materiaPrima = new MateriaPrima();
        materiaPrima.setNome("Matéria-prima teste");
        materiaPrima.setUnidadeMedida("un");
        materiaPrima.setEstoqueAtual(estoque);
        materiaPrima.setCustoUnitario(custo);
        return materiaPrima;
    }

    private static MovimentacaoMateriaPrima movimentacao(MateriaPrima materiaPrima, BigDecimal quantidade,
                                                            BigDecimal custoApurado) {
        MovimentacaoMateriaPrima movimentacao = new MovimentacaoMateriaPrima();
        movimentacao.setMateriaPrima(materiaPrima);
        movimentacao.setQuantidade(quantidade);
        movimentacao.setCustoUnitarioApurado(custoApurado);
        return movimentacao;
    }

    // --- criar (primeira compra) ---------------------------------------------------

    @Test
    void criar_calculaCustoUnitarioEDisparaDespesa() {
        MateriaPrimaRequest request = new MateriaPrimaRequest("Cera de soja", null, "kg",
                new BigDecimal("10"), new BigDecimal("150.00"), null, null);
        when(materiaPrimaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(movimentacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MateriaPrima resultado = service.criar(request);

        assertThat(resultado.getCustoUnitario()).isEqualByComparingTo(new BigDecimal("15.0000"));
        assertThat(resultado.getEstoqueAtual()).isEqualByComparingTo(new BigDecimal("10"));

        ArgumentCaptor<LancamentoFinanceiro> captor = ArgumentCaptor.forClass(LancamentoFinanceiro.class);
        verify(lancamentoFinanceiroRepository).save(captor.capture());
        assertThat(captor.getValue().getValor()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(captor.getValue().getTipo()).isEqualTo(TipoLancamento.DESPESA);
        assertThat(captor.getValue().getOrigem()).isEqualTo(OrigemLancamento.COMPRA);
    }

    // --- registrarMovimentacao -------------------------------------------------------

    @Test
    void registrarMovimentacao_entradaComValorPago_calculaCustoMedioPonderado() {
        MateriaPrima materiaPrima = materiaPrimaExistente(new BigDecimal("10"), new BigDecimal("15.0000"));
        when(materiaPrimaRepository.findById(1L)).thenReturn(Optional.of(materiaPrima));
        when(materiaPrimaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(movimentacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MovimentacaoMateriaPrimaRequest request = new MovimentacaoMateriaPrimaRequest(
                TipoMovimentacao.ENTRADA, MotivoMovimentacaoMateriaPrima.COMPRA,
                new BigDecimal("5"), new BigDecimal("100.00"), null);

        service.registrarMovimentacao(1L, request);

        // média ponderada: (10*15 + 5*20) / 15 = 250/15 = 16,6667
        assertThat(materiaPrima.getCustoUnitario()).isEqualByComparingTo(new BigDecimal("16.6667"));
        assertThat(materiaPrima.getEstoqueAtual()).isEqualByComparingTo(new BigDecimal("15"));
    }

    @Test
    void registrarMovimentacao_entradaSemValorPago_naoAlteraCustoNemLancaDespesa() {
        MateriaPrima materiaPrima = materiaPrimaExistente(new BigDecimal("10"), new BigDecimal("15.0000"));
        when(materiaPrimaRepository.findById(1L)).thenReturn(Optional.of(materiaPrima));
        when(materiaPrimaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(movimentacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MovimentacaoMateriaPrimaRequest request = new MovimentacaoMateriaPrimaRequest(
                TipoMovimentacao.ENTRADA, MotivoMovimentacaoMateriaPrima.PRODUCAO,
                new BigDecimal("3"), null, "ajuste de produção");

        service.registrarMovimentacao(1L, request);

        assertThat(materiaPrima.getCustoUnitario()).isEqualByComparingTo(new BigDecimal("15.0000"));
        assertThat(materiaPrima.getEstoqueAtual()).isEqualByComparingTo(new BigDecimal("13"));
        verifyNoInteractions(lancamentoFinanceiroRepository);
    }

    @Test
    void registrarMovimentacao_saidaMaiorQueEstoque_lancaExcecaoENaoSalvaNada() {
        MateriaPrima materiaPrima = materiaPrimaExistente(new BigDecimal("5"), new BigDecimal("10.00"));
        when(materiaPrimaRepository.findById(1L)).thenReturn(Optional.of(materiaPrima));

        MovimentacaoMateriaPrimaRequest request = new MovimentacaoMateriaPrimaRequest(
                TipoMovimentacao.SAIDA, MotivoMovimentacaoMateriaPrima.PERDA,
                new BigDecimal("10"), null, null);

        assertThatThrownBy(() -> service.registrarMovimentacao(1L, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Estoque insuficiente");

        verify(materiaPrimaRepository, never()).save(any());
        verify(movimentacaoRepository, never()).save(any());
    }

    @Test
    void registrarMovimentacao_valorPagoEmSaida_lancaExcecao() {
        MateriaPrima materiaPrima = materiaPrimaExistente(new BigDecimal("10"), new BigDecimal("10.00"));
        when(materiaPrimaRepository.findById(1L)).thenReturn(Optional.of(materiaPrima));

        MovimentacaoMateriaPrimaRequest request = new MovimentacaoMateriaPrimaRequest(
                TipoMovimentacao.SAIDA, MotivoMovimentacaoMateriaPrima.AJUSTE,
                new BigDecimal("2"), new BigDecimal("10.00"), null);

        assertThatThrownBy(() -> service.registrarMovimentacao(1L, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Valor pago só se aplica");
    }

    // --- excluir -----------------------------------------------------------------

    @Test
    void excluir_comMovimentacaoVinculada_lancaExcecaoENaoExclui() {
        MateriaPrima materiaPrima = materiaPrimaExistente(BigDecimal.TEN, BigDecimal.TEN);
        materiaPrima.setNome("Cera");
        when(materiaPrimaRepository.findById(1L)).thenReturn(Optional.of(materiaPrima));
        when(movimentacaoRepository.countByMateriaPrimaId(1L)).thenReturn(2L);
        when(receitaRepository.countByItens_MateriaPrimaId(1L)).thenReturn(0L);

        assertThatThrownBy(() -> service.excluir(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("movimentações de estoque");

        verify(materiaPrimaRepository, never()).delete(any());
    }

    @Test
    void excluir_semVinculo_excluiNormalmente() {
        MateriaPrima materiaPrima = materiaPrimaExistente(BigDecimal.ZERO, BigDecimal.ZERO);
        when(materiaPrimaRepository.findById(1L)).thenReturn(Optional.of(materiaPrima));
        when(movimentacaoRepository.countByMateriaPrimaId(1L)).thenReturn(0L);
        when(receitaRepository.countByItens_MateriaPrimaId(1L)).thenReturn(0L);

        service.excluir(1L);

        verify(materiaPrimaRepository).delete(materiaPrima);
    }

    // --- registrarEntradaVinculadaAConta -------------------------------------------

    @Test
    void registrarEntradaVinculadaAConta_calculaCustoMedioENaoLancaDespesa() {
        MateriaPrima materiaPrima = materiaPrimaExistente(new BigDecimal("10"), new BigDecimal("15.0000"));
        when(materiaPrimaRepository.findById(1L)).thenReturn(Optional.of(materiaPrima));
        when(materiaPrimaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        ArgumentCaptor<MovimentacaoMateriaPrima> captor = ArgumentCaptor.forClass(MovimentacaoMateriaPrima.class);
        when(movimentacaoRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        service.registrarEntradaVinculadaAConta(1L, new BigDecimal("5"), new BigDecimal("100.00"), 42L, null);

        assertThat(materiaPrima.getCustoUnitario()).isEqualByComparingTo(new BigDecimal("16.6667"));
        assertThat(materiaPrima.getEstoqueAtual()).isEqualByComparingTo(new BigDecimal("15"));
        assertThat(captor.getValue().getContaId()).isEqualTo(42L);
        assertThat(captor.getValue().getGrupoParcelamentoId()).isNull();
        // a despesa só nasce quando a conta é paga (ContaService), não aqui
        verifyNoInteractions(lancamentoFinanceiroRepository);
    }

    // --- existeCompraVinculada -----------------------------------------------------

    @Test
    void existeCompraVinculada_comGrupo_delegaParaGrupo() {
        UUID grupo = UUID.randomUUID();
        when(movimentacaoRepository.existsByGrupoParcelamentoId(grupo)).thenReturn(true);

        assertThat(service.existeCompraVinculada(99L, grupo)).isTrue();
        verify(movimentacaoRepository, never()).existsByContaId(any());
    }

    @Test
    void existeCompraVinculada_semGrupo_delegaParaConta() {
        when(movimentacaoRepository.existsByContaId(7L)).thenReturn(true);

        assertThat(service.existeCompraVinculada(7L, null)).isTrue();
        verify(movimentacaoRepository, never()).existsByGrupoParcelamentoId(any());
    }

    // --- estorno de compra vinculada a conta -----------------------------------------

    @Test
    void estornarComprasVinculadasAConta_reverteEstoqueECustoMedio() {
        MateriaPrima materiaPrima = materiaPrimaExistente(new BigDecimal("20"), new BigDecimal("10.0000"));
        materiaPrima.setId(1L);
        MovimentacaoMateriaPrima movimentacao = movimentacao(materiaPrima, new BigDecimal("5"), new BigDecimal("8.0000"));
        movimentacao.setId(100L);

        when(movimentacaoRepository.findByContaId(42L)).thenReturn(List.of(movimentacao));
        when(movimentacaoRepository.existsByMateriaPrimaIdAndIdGreaterThan(1L, 100L)).thenReturn(false);
        when(materiaPrimaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.estornarComprasVinculadasAConta(42L);

        // estoqueAntes = 20-5 = 15; custoAntes = (10*20 - 5*8)/15 = 160/15 = 10,6667
        assertThat(materiaPrima.getEstoqueAtual()).isEqualByComparingTo(new BigDecimal("15"));
        assertThat(materiaPrima.getCustoUnitario()).isEqualByComparingTo(new BigDecimal("10.6667"));
        verify(movimentacaoRepository).delete(movimentacao);
    }

    @Test
    void estornarComprasVinculadasAConta_quandoEstoqueVoltaAZero_zeraCustoUnitario() {
        MateriaPrima materiaPrima = materiaPrimaExistente(new BigDecimal("5"), new BigDecimal("12.0000"));
        materiaPrima.setId(1L);
        MovimentacaoMateriaPrima movimentacao = movimentacao(materiaPrima, new BigDecimal("5"), new BigDecimal("12.0000"));
        movimentacao.setId(100L);

        when(movimentacaoRepository.findByContaId(42L)).thenReturn(List.of(movimentacao));
        when(movimentacaoRepository.existsByMateriaPrimaIdAndIdGreaterThan(1L, 100L)).thenReturn(false);
        when(materiaPrimaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.estornarComprasVinculadasAConta(42L);

        assertThat(materiaPrima.getEstoqueAtual()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(materiaPrima.getCustoUnitario()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void estornarComprasVinculadasAConta_comMovimentacaoMaisNova_lancaConflitoENaoAplicaNada() {
        MateriaPrima materiaPrima = materiaPrimaExistente(new BigDecimal("20"), new BigDecimal("10.0000"));
        materiaPrima.setId(1L);
        materiaPrima.setNome("Cera");
        MovimentacaoMateriaPrima movimentacao = movimentacao(materiaPrima, new BigDecimal("5"), new BigDecimal("8.0000"));
        movimentacao.setId(100L);

        when(movimentacaoRepository.findByContaId(42L)).thenReturn(List.of(movimentacao));
        when(movimentacaoRepository.existsByMateriaPrimaIdAndIdGreaterThan(1L, 100L)).thenReturn(true);

        assertThatThrownBy(() -> service.estornarComprasVinculadasAConta(42L))
                .isInstanceOf(ConflitoOperacaoException.class)
                .hasMessageContaining("já houve outra movimentação");

        verify(movimentacaoRepository, never()).delete(any());
        verify(materiaPrimaRepository, never()).save(any());
    }

    @Test
    void estornarComprasVinculadasAConta_deixariaEstoqueNegativo_lancaConflito() {
        MateriaPrima materiaPrima = materiaPrimaExistente(new BigDecimal("3"), new BigDecimal("10.0000"));
        materiaPrima.setId(1L);
        materiaPrima.setNome("Cera");
        MovimentacaoMateriaPrima movimentacao = movimentacao(materiaPrima, new BigDecimal("5"), new BigDecimal("8.0000"));
        movimentacao.setId(100L);

        when(movimentacaoRepository.findByContaId(42L)).thenReturn(List.of(movimentacao));
        when(movimentacaoRepository.existsByMateriaPrimaIdAndIdGreaterThan(1L, 100L)).thenReturn(false);

        assertThatThrownBy(() -> service.estornarComprasVinculadasAConta(42L))
                .isInstanceOf(ConflitoOperacaoException.class)
                .hasMessageContaining("estoque de 'Cera' negativo");

        verify(materiaPrimaRepository, never()).save(any());
    }

    @Test
    void estornarComprasVinculadasAGrupo_comDuasMovimentacoesDaMesmaMateriaPrima_reverteDaMaisNovaPraMaisVelha() {
        MateriaPrima materiaPrima = materiaPrimaExistente(new BigDecimal("30"), new BigDecimal("10.0000"));
        materiaPrima.setId(1L);
        MovimentacaoMateriaPrima movA = movimentacao(materiaPrima, new BigDecimal("5"), new BigDecimal("8.0000"));
        movA.setId(100L);
        MovimentacaoMateriaPrima movB = movimentacao(materiaPrima, new BigDecimal("10"), new BigDecimal("9.0000"));
        movB.setId(101L);

        UUID grupo = UUID.randomUUID();
        when(movimentacaoRepository.findByGrupoParcelamentoId(grupo)).thenReturn(List.of(movA, movB));
        // "aconteceu algo depois?" é checado contra o MAIOR id do lote (101), pras duas
        when(movimentacaoRepository.existsByMateriaPrimaIdAndIdGreaterThan(1L, 101L)).thenReturn(false);
        when(materiaPrimaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.estornarComprasVinculadasAGrupo(grupo);

        // reverte 101 primeiro: estoque 30->20, custo (10*30-10*9)/20 = 210/20 = 10,5000
        // depois 100: estoque 20->15, custo (10.5*20-5*8)/15 = 170/15 = 11,3333
        assertThat(materiaPrima.getEstoqueAtual()).isEqualByComparingTo(new BigDecimal("15"));
        assertThat(materiaPrima.getCustoUnitario()).isEqualByComparingTo(new BigDecimal("11.3333"));
    }
}
