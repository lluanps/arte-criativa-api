package br.com.artecriativa.api.financeiro;

import br.com.artecriativa.api.estoque.MateriaPrimaService;
import br.com.artecriativa.api.financeiro.dto.ContaParceladaRequest;
import br.com.artecriativa.api.financeiro.dto.ContaRequest;
import br.com.artecriativa.api.financeiro.dto.ItemMateriaPrimaCompraRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Cobre a lógica delicada de {@link ContaService}: rateio exato do parcelamento
 * (soma sempre bate com o total, resto do arredondamento na última parcela),
 * bloqueio de edição de valor numa conta já vinculada a compra de matéria-prima, e a
 * exclusão — que só estorna estoque quando é a ÚLTIMA parcela restante do grupo.
 */
@ExtendWith(MockitoExtension.class)
class ContaServiceTest {

    @Mock
    private ContaRepository contaRepository;
    @Mock
    private LancamentoFinanceiroRepository lancamentoFinanceiroRepository;
    @Mock
    private MateriaPrimaService materiaPrimaService;

    @InjectMocks
    private ContaService contaService;

    private static Conta contaExistente(Long id, BigDecimal valor, UUID grupoParcelamentoId) {
        Conta conta = new Conta();
        conta.setId(id);
        conta.setTipo(TipoConta.PAGAR);
        conta.setDescricao("Conta teste");
        conta.setValor(valor);
        conta.setVencimento(LocalDate.of(2026, 1, 1));
        conta.setGrupoParcelamentoId(grupoParcelamentoId);
        return conta;
    }

    // --- criarParcelada: rateio ---------------------------------------------------

    @Test
    void criarParcelada_divideValorIgualmenteEJogaRestoNaUltima() {
        lenient().when(contaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        ContaParceladaRequest request = new ContaParceladaRequest(
                TipoConta.PAGAR, "Compra teste", new BigDecimal("100.00"), 3,
                LocalDate.of(2026, 1, 10), null, null);

        List<Conta> parcelas = contaService.criarParcelada(request);

        assertThat(parcelas).hasSize(3);
        assertThat(parcelas.get(0).getValor()).isEqualByComparingTo(new BigDecimal("33.33"));
        assertThat(parcelas.get(1).getValor()).isEqualByComparingTo(new BigDecimal("33.33"));
        assertThat(parcelas.get(2).getValor()).isEqualByComparingTo(new BigDecimal("33.34"));

        BigDecimal soma = parcelas.stream().map(Conta::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(soma).isEqualByComparingTo(new BigDecimal("100.00"));

        assertThat(parcelas.get(0).getVencimento()).isEqualTo(LocalDate.of(2026, 1, 10));
        assertThat(parcelas.get(2).getVencimento()).isEqualTo(LocalDate.of(2026, 3, 10));
        assertThat(parcelas.get(0).getDescricao()).isEqualTo("Compra teste (parcela 1/3)");

        UUID grupo = parcelas.get(0).getGrupoParcelamentoId();
        assertThat(grupo).isNotNull();
        assertThat(parcelas).allSatisfy(c -> assertThat(c.getGrupoParcelamentoId()).isEqualTo(grupo));
    }

    @Test
    void criarParcelada_comItens_validaSomaEVinculaAoGrupoInteiro() {
        lenient().when(contaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        List<ItemMateriaPrimaCompraRequest> itens = List.of(
                new ItemMateriaPrimaCompraRequest(1L, new BigDecimal("10"), new BigDecimal("70.00")),
                new ItemMateriaPrimaCompraRequest(2L, new BigDecimal("5"), new BigDecimal("30.00")));
        ContaParceladaRequest request = new ContaParceladaRequest(
                TipoConta.PAGAR, "Kit", new BigDecimal("100.00"), 2,
                LocalDate.of(2026, 1, 10), itens, null);

        contaService.criarParcelada(request);

        ArgumentCaptor<UUID> grupoCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(materiaPrimaService).registrarEntradaVinculadaAConta(
                eq(1L), eq(new BigDecimal("10")), eq(new BigDecimal("70.00")), isNull(), grupoCaptor.capture());
        verify(materiaPrimaService).registrarEntradaVinculadaAConta(
                eq(2L), eq(new BigDecimal("5")), eq(new BigDecimal("30.00")), isNull(), eq(grupoCaptor.getValue()));
    }

    @Test
    void criarParcelada_comItensSomaNaoBateComValorTotal_lancaExcecaoSemCriarNada() {
        List<ItemMateriaPrimaCompraRequest> itens = List.of(
                new ItemMateriaPrimaCompraRequest(1L, new BigDecimal("10"), new BigDecimal("50.00")));
        ContaParceladaRequest request = new ContaParceladaRequest(
                TipoConta.PAGAR, "Kit", new BigDecimal("100.00"), 2,
                LocalDate.of(2026, 1, 10), itens, null);

        assertThatThrownBy(() -> contaService.criarParcelada(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("não bate");

        verifyNoInteractions(contaRepository);
        verifyNoInteractions(materiaPrimaService);
    }

    // --- criar: itens de matéria-prima ---------------------------------------------

    @Test
    void criar_comItensEmContaReceber_lancaExcecao() {
        List<ItemMateriaPrimaCompraRequest> itens = List.of(
                new ItemMateriaPrimaCompraRequest(1L, BigDecimal.ONE, new BigDecimal("10.00")));
        ContaRequest request = new ContaRequest(TipoConta.RECEBER, "Venda de sobra", new BigDecimal("10.00"),
                LocalDate.of(2026, 1, 1), itens, null);

        assertThatThrownBy(() -> contaService.criar(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PAGAR");

        verifyNoInteractions(contaRepository);
    }

    @Test
    void criar_comCustosExtrasNaSoma_registraEntradaVinculadaComIdDaConta() {
        lenient().when(contaRepository.save(any())).thenAnswer(inv -> {
            Conta c = inv.getArgument(0);
            c.setId(9L);
            return c;
        });
        List<ItemMateriaPrimaCompraRequest> itens = List.of(
                new ItemMateriaPrimaCompraRequest(1L, new BigDecimal("10"), new BigDecimal("60.00")));
        ContaRequest request = new ContaRequest(TipoConta.PAGAR, "Compra", new BigDecimal("100.00"),
                LocalDate.of(2026, 1, 1), itens, new BigDecimal("40.00"));

        Conta conta = contaService.criar(request);

        assertThat(conta.getId()).isEqualTo(9L);
        verify(materiaPrimaService).registrarEntradaVinculadaAConta(
                1L, new BigDecimal("10"), new BigDecimal("60.00"), 9L, null);
    }

    // --- atualizar: bloqueio de valor em conta vinculada ----------------------------

    @Test
    void atualizar_valorDiferenteEmContaVinculada_lancaExcecaoENaoSalva() {
        Conta existente = contaExistente(5L, new BigDecimal("100.00"), null);
        when(contaRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(materiaPrimaService.existeCompraVinculada(5L, null)).thenReturn(true);

        ContaRequest request = new ContaRequest(TipoConta.PAGAR, "Compra", new BigDecimal("120.00"),
                LocalDate.of(2026, 1, 1), null, null);

        assertThatThrownBy(() -> contaService.atualizar(5L, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("exclua e crie novamente");

        verify(contaRepository, never()).save(any());
    }

    @Test
    void atualizar_mesmoValorEmContaVinculada_permiteEditarDescricao() {
        // valor igual -> o "&&" nem chega a checar existeCompraVinculada (curto-circuito)
        Conta existente = contaExistente(5L, new BigDecimal("100.00"), null);
        when(contaRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(contaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ContaRequest request = new ContaRequest(TipoConta.PAGAR, "Descrição corrigida", new BigDecimal("100.00"),
                LocalDate.of(2026, 1, 1), null, null);

        Conta atualizada = contaService.atualizar(5L, request);

        assertThat(atualizada.getDescricao()).isEqualTo("Descrição corrigida");
    }

    // --- marcarComoPaga --------------------------------------------------------------

    @Test
    void marcarComoPaga_contaJaPaga_lancaExcecao() {
        Conta conta = contaExistente(1L, new BigDecimal("50.00"), null);
        conta.setStatus(StatusConta.PAGO);
        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));

        assertThatThrownBy(() -> contaService.marcarComoPaga(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("já está paga");
    }

    @Test
    void marcarComoPaga_semVinculo_usaCategoriaGenericaContaAPagar() {
        Conta conta = contaExistente(1L, new BigDecimal("50.00"), null);
        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
        when(contaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(materiaPrimaService.existeCompraVinculada(1L, null)).thenReturn(false);
        when(lancamentoFinanceiroRepository.findByOrigemAndOrigemId(OrigemLancamento.CONTA, 1L))
                .thenReturn(Optional.empty());
        when(lancamentoFinanceiroRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        contaService.marcarComoPaga(1L);

        ArgumentCaptor<LancamentoFinanceiro> captor = ArgumentCaptor.forClass(LancamentoFinanceiro.class);
        verify(lancamentoFinanceiroRepository).save(captor.capture());
        assertThat(captor.getValue().getTipo()).isEqualTo(TipoLancamento.DESPESA);
        assertThat(captor.getValue().getCategoria()).isEqualTo("Conta a pagar");
    }

    @Test
    void marcarComoPaga_vinculadaAMateriaPrima_usaCategoriaCompraDeMateriaPrima() {
        Conta conta = contaExistente(1L, new BigDecimal("50.00"), null);
        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
        when(contaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(materiaPrimaService.existeCompraVinculada(1L, null)).thenReturn(true);
        when(lancamentoFinanceiroRepository.findByOrigemAndOrigemId(OrigemLancamento.CONTA, 1L))
                .thenReturn(Optional.empty());
        when(lancamentoFinanceiroRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        contaService.marcarComoPaga(1L);

        ArgumentCaptor<LancamentoFinanceiro> captor = ArgumentCaptor.forClass(LancamentoFinanceiro.class);
        verify(lancamentoFinanceiroRepository).save(captor.capture());
        assertThat(captor.getValue().getCategoria()).isEqualTo("Compra de matéria-prima");
    }

    // --- excluir: estorno só na última parcela do grupo -----------------------------

    @Test
    void excluir_contaJaPaga_removeLancamentoFinanceiroJunto() {
        Conta conta = contaExistente(3L, new BigDecimal("50.00"), null);
        LancamentoFinanceiro lancamento = new LancamentoFinanceiro();
        when(contaRepository.findById(3L)).thenReturn(Optional.of(conta));
        when(lancamentoFinanceiroRepository.findByOrigemAndOrigemId(OrigemLancamento.CONTA, 3L))
                .thenReturn(Optional.of(lancamento));

        contaService.excluir(3L);

        verify(lancamentoFinanceiroRepository).delete(lancamento);
    }

    @Test
    void excluir_contaAvulsa_estornaComprasDaConta() {
        Conta conta = contaExistente(3L, new BigDecimal("50.00"), null);
        when(contaRepository.findById(3L)).thenReturn(Optional.of(conta));
        when(lancamentoFinanceiroRepository.findByOrigemAndOrigemId(OrigemLancamento.CONTA, 3L))
                .thenReturn(Optional.empty());

        contaService.excluir(3L);

        verify(materiaPrimaService).estornarComprasVinculadasAConta(3L);
        verify(materiaPrimaService, never()).estornarComprasVinculadasAGrupo(any());
        verify(contaRepository).delete(conta);
    }

    @Test
    void excluir_ultimaParcelaRestanteDoGrupo_estornaComprasDoGrupoInteiro() {
        UUID grupo = UUID.randomUUID();
        Conta conta = contaExistente(4L, new BigDecimal("50.00"), grupo);
        when(contaRepository.findById(4L)).thenReturn(Optional.of(conta));
        when(lancamentoFinanceiroRepository.findByOrigemAndOrigemId(OrigemLancamento.CONTA, 4L))
                .thenReturn(Optional.empty());
        when(contaRepository.countByGrupoParcelamentoId(grupo)).thenReturn(1L);

        contaService.excluir(4L);

        verify(materiaPrimaService).estornarComprasVinculadasAGrupo(grupo);
        verify(materiaPrimaService, never()).estornarComprasVinculadasAConta(any());
    }

    @Test
    void excluir_parcelaComOutrasRestantesNoGrupo_naoEstornaEstoqueAinda() {
        UUID grupo = UUID.randomUUID();
        Conta conta = contaExistente(4L, new BigDecimal("50.00"), grupo);
        when(contaRepository.findById(4L)).thenReturn(Optional.of(conta));
        when(lancamentoFinanceiroRepository.findByOrigemAndOrigemId(OrigemLancamento.CONTA, 4L))
                .thenReturn(Optional.empty());
        when(contaRepository.countByGrupoParcelamentoId(grupo)).thenReturn(2L);

        contaService.excluir(4L);

        verify(materiaPrimaService, never()).estornarComprasVinculadasAGrupo(any());
        verify(materiaPrimaService, never()).estornarComprasVinculadasAConta(any());
    }
}
