package br.com.artecriativa.api.vendas;

import br.com.artecriativa.api.cadastros.CanalVendaRepository;
import br.com.artecriativa.api.cadastros.ClienteRepository;
import br.com.artecriativa.api.estoque.MovimentacaoProdutoRepository;
import br.com.artecriativa.api.estoque.Produto;
import br.com.artecriativa.api.estoque.ProdutoRepository;
import br.com.artecriativa.api.financeiro.LancamentoFinanceiro;
import br.com.artecriativa.api.financeiro.LancamentoFinanceiroRepository;
import br.com.artecriativa.api.financeiro.OrigemLancamento;
import br.com.artecriativa.api.financeiro.TipoLancamento;
import br.com.artecriativa.api.vendas.dto.VendaItemRequest;
import br.com.artecriativa.api.vendas.dto.VendaRequest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Cobre a lógica de "encomendas com status e prazo": a venda de balcão (sem data de
 * entrega) precisa continuar se comportando exatamente como antes (não-regressão), e a
 * encomenda precisa lançar só o sinal na criação, e o saldo só ao chegar em ENTREGUE.
 */
@ExtendWith(MockitoExtension.class)
class VendaServiceTest {

    @Mock
    private VendaRepository vendaRepository;
    @Mock
    private ProdutoRepository produtoRepository;
    @Mock
    private MovimentacaoProdutoRepository movimentacaoProdutoRepository;
    @Mock
    private LancamentoFinanceiroRepository lancamentoFinanceiroRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private CanalVendaRepository canalVendaRepository;

    @InjectMocks
    private VendaService vendaService;

    private static Produto produtoExistente(Long id, BigDecimal estoque, BigDecimal preco) {
        Produto produto = new Produto();
        produto.setId(id);
        produto.setNome("Produto teste");
        produto.setEstoqueAtual(estoque);
        produto.setPrecoVenda(preco);
        return produto;
    }

    private static VendaRequest requestComUmItem(LocalDate dataEntregaPrevista, BigDecimal valorSinal) {
        return new VendaRequest(null, null,
                List.of(new VendaItemRequest(1L, BigDecimal.TEN, null)),
                dataEntregaPrevista, valorSinal);
    }

    private void mockularSalvamentoDeProdutoEVenda() {
        lenient().when(produtoRepository.findById(1L)).thenReturn(Optional.of(produtoExistente(1L, new BigDecimal("100"), new BigDecimal("10.00"))));
        lenient().when(produtoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(vendaRepository.save(any())).thenAnswer(inv -> {
            Venda venda = inv.getArgument(0);
            if (venda.getId() == null) {
                venda.setId(1L);
            }
            return venda;
        });
        lenient().when(lancamentoFinanceiroRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private static Venda encomendaExistente(Long id, StatusVenda status, BigDecimal valorTotal, BigDecimal valorSinal,
                                              LocalDate dataEntregaPrevista) {
        Venda venda = new Venda();
        venda.setId(id);
        venda.setStatus(status);
        venda.setValorTotal(valorTotal);
        venda.setValorSinal(valorSinal);
        venda.setDataEntregaPrevista(dataEntregaPrevista);
        return venda;
    }

    // --- registrar: não-regressão / venda de balcão ---------------------------------

    @Test
    void registrar_semDataEntrega_comportamentoAtualInalterado() {
        mockularSalvamentoDeProdutoEVenda();
        VendaRequest request = requestComUmItem(null, null);

        Venda venda = vendaService.registrar(request);

        assertThat(venda.getStatus()).isEqualTo(StatusVenda.ENTREGUE);
        assertThat(venda.getDataEntregaPrevista()).isNull();
        assertThat(venda.getValorSinal()).isEqualByComparingTo(BigDecimal.ZERO);

        ArgumentCaptor<LancamentoFinanceiro> captor = ArgumentCaptor.forClass(LancamentoFinanceiro.class);
        verify(lancamentoFinanceiroRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getValor()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(captor.getValue().getTipo()).isEqualTo(TipoLancamento.RECEITA);
    }

    // --- registrar: encomenda --------------------------------------------------------

    @Test
    void registrar_comDataEntregaSemSinal_nascePendenteSemGerarLancamento() {
        mockularSalvamentoDeProdutoEVenda();
        VendaRequest request = requestComUmItem(LocalDate.of(2026, 9, 10), null);

        Venda venda = vendaService.registrar(request);

        assertThat(venda.getStatus()).isEqualTo(StatusVenda.PENDENTE);
        verify(lancamentoFinanceiroRepository, never()).save(any());
    }

    @Test
    void registrar_comDataEntregaESinal_geraLancamentoSoDoSinal() {
        mockularSalvamentoDeProdutoEVenda();
        VendaRequest request = requestComUmItem(LocalDate.of(2026, 9, 10), new BigDecimal("30.00"));

        vendaService.registrar(request);

        ArgumentCaptor<LancamentoFinanceiro> captor = ArgumentCaptor.forClass(LancamentoFinanceiro.class);
        verify(lancamentoFinanceiroRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getValor()).isEqualByComparingTo(new BigDecimal("30.00"));
        assertThat(captor.getValue().getDescricao()).contains("(sinal)");
    }

    @Test
    void registrar_sinalMaiorQueTotal_lancaExcecao() {
        lenient().when(produtoRepository.findById(1L)).thenReturn(Optional.of(produtoExistente(1L, new BigDecimal("100"), new BigDecimal("10.00"))));
        VendaRequest request = requestComUmItem(LocalDate.of(2026, 9, 10), new BigDecimal("999.00"));

        assertThatThrownBy(() -> vendaService.registrar(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("maior que o valor total");

        verify(vendaRepository, never()).save(any());
    }

    @Test
    void registrar_sinalSemDataEntrega_lancaExcecao() {
        lenient().when(produtoRepository.findById(1L)).thenReturn(Optional.of(produtoExistente(1L, new BigDecimal("100"), new BigDecimal("10.00"))));
        VendaRequest request = requestComUmItem(null, new BigDecimal("10.00"));

        assertThatThrownBy(() -> vendaService.registrar(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Sinal só é aplicável");

        verify(vendaRepository, never()).save(any());
    }

    // --- avancarStatus -----------------------------------------------------------------

    @Test
    void avancarStatus_avancaUmEstagioSemGerarLancamentoAntesDeEntregue() {
        Venda venda = encomendaExistente(1L, StatusVenda.PENDENTE, new BigDecimal("100.00"), new BigDecimal("30.00"),
                LocalDate.of(2026, 9, 10));
        when(vendaRepository.findById(1L)).thenReturn(Optional.of(venda));
        when(vendaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Venda atualizada = vendaService.avancarStatus(1L);

        assertThat(atualizada.getStatus()).isEqualTo(StatusVenda.EM_PRODUCAO);
        verify(lancamentoFinanceiroRepository, never()).save(any());
    }

    @Test
    void avancarStatus_aoChegarEntregueComSaldoPendente_geraLancamentoDoSaldo() {
        Venda venda = encomendaExistente(1L, StatusVenda.PRONTO, new BigDecimal("100.00"), new BigDecimal("30.00"),
                LocalDate.of(2026, 9, 10));
        when(vendaRepository.findById(1L)).thenReturn(Optional.of(venda));
        when(vendaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(lancamentoFinanceiroRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Venda atualizada = vendaService.avancarStatus(1L);

        assertThat(atualizada.getStatus()).isEqualTo(StatusVenda.ENTREGUE);
        ArgumentCaptor<LancamentoFinanceiro> captor = ArgumentCaptor.forClass(LancamentoFinanceiro.class);
        verify(lancamentoFinanceiroRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getValor()).isEqualByComparingTo(new BigDecimal("70.00"));
        assertThat(captor.getValue().getDescricao()).contains("(saldo na entrega)");
    }

    @Test
    void avancarStatus_aoChegarEntregueSemSaldo_naoGeraLancamento() {
        Venda venda = encomendaExistente(1L, StatusVenda.PRONTO, new BigDecimal("100.00"), new BigDecimal("100.00"),
                LocalDate.of(2026, 9, 10));
        when(vendaRepository.findById(1L)).thenReturn(Optional.of(venda));
        when(vendaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        vendaService.avancarStatus(1L);

        verify(lancamentoFinanceiroRepository, never()).save(any());
    }

    @Test
    void avancarStatus_vendaSemDataEntrega_lancaExcecao() {
        Venda venda = encomendaExistente(1L, StatusVenda.ENTREGUE, new BigDecimal("100.00"), BigDecimal.ZERO, null);
        when(vendaRepository.findById(1L)).thenReturn(Optional.of(venda));

        assertThatThrownBy(() -> vendaService.avancarStatus(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("não é uma encomenda");
    }

    @Test
    void avancarStatus_jaEntregue_lancaExcecao() {
        Venda venda = encomendaExistente(1L, StatusVenda.ENTREGUE, new BigDecimal("100.00"), BigDecimal.ZERO,
                LocalDate.of(2026, 9, 10));
        when(vendaRepository.findById(1L)).thenReturn(Optional.of(venda));

        assertThatThrownBy(() -> vendaService.avancarStatus(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("já está entregue");
    }

    // --- reagendarEntrega ----------------------------------------------------------

    @Test
    void reagendarEntrega_vendaSemDataEntrega_lancaExcecao() {
        Venda venda = encomendaExistente(1L, StatusVenda.ENTREGUE, new BigDecimal("100.00"), BigDecimal.ZERO, null);
        when(vendaRepository.findById(1L)).thenReturn(Optional.of(venda));

        assertThatThrownBy(() -> vendaService.reagendarEntrega(1L, LocalDate.of(2026, 10, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("não é uma encomenda");
    }

    @Test
    void reagendarEntrega_vendaJaEntregue_lancaExcecao() {
        Venda venda = encomendaExistente(1L, StatusVenda.ENTREGUE, new BigDecimal("100.00"), BigDecimal.ZERO,
                LocalDate.of(2026, 9, 10));
        when(vendaRepository.findById(1L)).thenReturn(Optional.of(venda));

        assertThatThrownBy(() -> vendaService.reagendarEntrega(1L, LocalDate.of(2026, 10, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("já está entregue");
    }

    // --- excluir: cascade de lançamentos ---------------------------------------------

    @Test
    void excluir_comSinalESaldoLancados_removeAmbosOsLancamentos() {
        Venda venda = encomendaExistente(2L, StatusVenda.ENTREGUE, new BigDecimal("100.00"), new BigDecimal("30.00"),
                LocalDate.of(2026, 9, 10));
        List<LancamentoFinanceiro> lancamentos = List.of(new LancamentoFinanceiro(), new LancamentoFinanceiro());
        when(vendaRepository.findById(2L)).thenReturn(Optional.of(venda));
        when(lancamentoFinanceiroRepository.findAllByOrigemAndOrigemId(OrigemLancamento.VENDA, 2L))
                .thenReturn(lancamentos);

        vendaService.excluir(2L);

        verify(lancamentoFinanceiroRepository).deleteAll(lancamentos);
        verify(vendaRepository).delete(venda);
    }

    @Test
    void excluir_encomendaSemNenhumLancamentoAindaGerado_naoQuebra() {
        Venda venda = encomendaExistente(3L, StatusVenda.PENDENTE, new BigDecimal("100.00"), BigDecimal.ZERO,
                LocalDate.of(2026, 9, 10));
        when(vendaRepository.findById(3L)).thenReturn(Optional.of(venda));
        when(lancamentoFinanceiroRepository.findAllByOrigemAndOrigemId(OrigemLancamento.VENDA, 3L))
                .thenReturn(List.of());

        vendaService.excluir(3L);

        verify(lancamentoFinanceiroRepository).deleteAll(List.of());
        verify(vendaRepository).delete(venda);
    }
}
