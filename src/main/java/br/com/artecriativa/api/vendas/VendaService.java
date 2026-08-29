package br.com.artecriativa.api.vendas;

import br.com.artecriativa.api.cadastros.CanalVenda;
import br.com.artecriativa.api.cadastros.CanalVendaRepository;
import br.com.artecriativa.api.cadastros.Cliente;
import br.com.artecriativa.api.cadastros.ClienteRepository;
import br.com.artecriativa.api.common.FormatoNumerico;
import br.com.artecriativa.api.common.RecursoNaoEncontradoException;
import br.com.artecriativa.api.estoque.MotivoMovimentacaoProduto;
import br.com.artecriativa.api.estoque.MovimentacaoProduto;
import br.com.artecriativa.api.estoque.MovimentacaoProdutoRepository;
import br.com.artecriativa.api.estoque.Produto;
import br.com.artecriativa.api.estoque.ProdutoRepository;
import br.com.artecriativa.api.estoque.TipoMovimentacao;
import br.com.artecriativa.api.financeiro.LancamentoFinanceiro;
import br.com.artecriativa.api.financeiro.LancamentoFinanceiroRepository;
import br.com.artecriativa.api.financeiro.OrigemLancamento;
import br.com.artecriativa.api.financeiro.TipoLancamento;
import br.com.artecriativa.api.vendas.dto.VendaItemRequest;
import br.com.artecriativa.api.vendas.dto.VendaRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Registra uma venda: dá baixa no estoque de cada produto vendido (com movimentação de
 * saída por VENDA), calcula o valor total e gera um lançamento financeiro de receita
 * correspondente.
 */
@Service
@RequiredArgsConstructor
public class VendaService {

    private final VendaRepository vendaRepository;
    private final ProdutoRepository produtoRepository;
    private final MovimentacaoProdutoRepository movimentacaoProdutoRepository;
    private final LancamentoFinanceiroRepository lancamentoFinanceiroRepository;
    private final ClienteRepository clienteRepository;
    private final CanalVendaRepository canalVendaRepository;

    @Transactional(readOnly = true)
    public List<Venda> listarTodas() {
        return vendaRepository.findAllByOrderByDataVendaDesc();
    }

    @Transactional(readOnly = true)
    public Venda buscarPorId(Long id) {
        return vendaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Venda não encontrada: " + id));
    }

    @Transactional(readOnly = true)
    public List<Venda> listarPorCliente(Long clienteId) {
        return vendaRepository.findByClienteIdOrderByDataVendaDesc(clienteId);
    }

    @Transactional
    public Venda registrar(VendaRequest request) {
        Cliente cliente = buscarCliente(request.clienteId());
        CanalVenda canal = buscarCanal(request.canalId());

        List<VendaItem> itens = new ArrayList<>();
        BigDecimal valorTotal = BigDecimal.ZERO;

        for (VendaItemRequest itemRequest : request.itens()) {
            Produto produto = produtoRepository.findById(itemRequest.produtoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Produto não encontrado: " + itemRequest.produtoId()));

            BigDecimal novoEstoque = produto.getEstoqueAtual().subtract(itemRequest.quantidade());
            if (novoEstoque.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalStateException(
                        "Estoque insuficiente do produto '%s'. Disponível: %s, solicitado: %s"
                                .formatted(produto.getNome(),
                                        FormatoNumerico.semZerosDesnecessarios(produto.getEstoqueAtual()),
                                        FormatoNumerico.semZerosDesnecessarios(itemRequest.quantidade())));
            }

            BigDecimal precoUnitario = itemRequest.precoUnitario() != null
                    ? itemRequest.precoUnitario()
                    : produto.getPrecoVenda();

            produto.setEstoqueAtual(novoEstoque);
            produtoRepository.save(produto);

            MovimentacaoProduto movimentacao = new MovimentacaoProduto();
            movimentacao.setProduto(produto);
            movimentacao.setTipo(TipoMovimentacao.SAIDA);
            movimentacao.setMotivo(MotivoMovimentacaoProduto.VENDA);
            movimentacao.setQuantidade(itemRequest.quantidade());
            movimentacao.setObservacao("Venda" + (cliente != null ? " para " + cliente.getNome() : ""));
            movimentacaoProdutoRepository.save(movimentacao);

            VendaItem item = new VendaItem();
            item.setProduto(produto);
            item.setQuantidade(itemRequest.quantidade());
            item.setPrecoUnitario(precoUnitario);
            itens.add(item);

            valorTotal = valorTotal.add(item.getSubtotal());
        }

        boolean encomenda = request.dataEntregaPrevista() != null;
        BigDecimal valorSinal = request.valorSinal() != null ? request.valorSinal() : BigDecimal.ZERO;
        if (!encomenda && valorSinal.compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException(
                    "Sinal só é aplicável a encomenda com data de entrega informada.");
        }
        if (encomenda && valorSinal.compareTo(valorTotal) > 0) {
            throw new IllegalStateException("Sinal não pode ser maior que o valor total da venda.");
        }

        Venda venda = new Venda();
        venda.setCliente(cliente);
        venda.setCanal(canal);
        venda.setValorTotal(valorTotal);
        venda.adicionarItens(itens);
        venda.setDataEntregaPrevista(request.dataEntregaPrevista());
        venda.setValorSinal(valorSinal);
        venda.setStatus(encomenda ? StatusVenda.PENDENTE : StatusVenda.ENTREGUE);
        venda = vendaRepository.save(venda);

        if (!encomenda) {
            criarLancamentoReceita(venda, cliente, valorTotal, "");
        } else if (valorSinal.compareTo(BigDecimal.ZERO) > 0) {
            criarLancamentoReceita(venda, cliente, valorSinal, " (sinal)");
        }

        return venda;
    }

    /**
     * Avança a encomenda pro próximo estágio de {@link StatusVenda}, em sequência
     * (PENDENTE → EM_PRODUCAO → PRONTO → ENTREGUE). Só ao chegar em ENTREGUE é que o
     * saldo (total - sinal já recebido) vira lançamento financeiro, se houver saldo —
     * antes disso a encomenda pode estar com pagamento parcial ou nenhum ainda.
     */
    @Transactional
    public Venda avancarStatus(Long id) {
        Venda venda = buscarPorId(id);
        if (venda.getDataEntregaPrevista() == null) {
            throw new IllegalStateException("Venda #%d não é uma encomenda (sem data de entrega).".formatted(id));
        }
        if (venda.getStatus() == StatusVenda.ENTREGUE) {
            throw new IllegalStateException("Encomenda #%d já está entregue.".formatted(id));
        }

        StatusVenda proximo = StatusVenda.values()[venda.getStatus().ordinal() + 1];
        venda.setStatus(proximo);
        venda = vendaRepository.save(venda);

        if (proximo == StatusVenda.ENTREGUE) {
            BigDecimal saldo = venda.getValorSaldo();
            if (saldo.compareTo(BigDecimal.ZERO) > 0) {
                criarLancamentoReceita(venda, venda.getCliente(), saldo, " (saldo na entrega)");
            }
        }

        return venda;
    }

    /** Reagenda a data de entrega combinada — não muda o status nem o financeiro. */
    @Transactional
    public Venda reagendarEntrega(Long id, LocalDate novaData) {
        Venda venda = buscarPorId(id);
        if (venda.getDataEntregaPrevista() == null) {
            throw new IllegalStateException("Venda #%d não é uma encomenda (sem data de entrega).".formatted(id));
        }
        if (venda.getStatus() == StatusVenda.ENTREGUE) {
            throw new IllegalStateException("Encomenda #%d já está entregue.".formatted(id));
        }
        venda.setDataEntregaPrevista(novaData);
        return vendaRepository.save(venda);
    }

    private void criarLancamentoReceita(Venda venda, Cliente cliente, BigDecimal valor, String sufixoDescricao) {
        LancamentoFinanceiro lancamento = new LancamentoFinanceiro();
        lancamento.setTipo(TipoLancamento.RECEITA);
        lancamento.setCategoria("Venda");
        lancamento.setValor(valor);
        lancamento.setDescricao(
                "Venda #" + venda.getId() + (cliente != null ? " - " + cliente.getNome() : "") + sufixoDescricao);
        lancamento.setOrigem(OrigemLancamento.VENDA);
        lancamento.setOrigemId(venda.getId());
        lancamentoFinanceiroRepository.save(lancamento);
    }

    /**
     * Exclui a venda estornando os efeitos colaterais do registro original: devolve a
     * quantidade de cada item ao estoque (com uma movimentação de AJUSTE explicando o
     * estorno) e remove o(s) lançamento(s) financeiro(s) de receita gerado(s) por ela —
     * uma encomenda pode ter até 2 (sinal + saldo). Sem isso, o estoque e o financeiro
     * ficariam desalinhados com a realidade.
     */
    @Transactional
    public void excluir(Long id) {
        Venda venda = buscarPorId(id);

        for (VendaItem item : venda.getItens()) {
            Produto produto = item.getProduto();
            produto.setEstoqueAtual(produto.getEstoqueAtual().add(item.getQuantidade()));
            produtoRepository.save(produto);

            MovimentacaoProduto estorno = new MovimentacaoProduto();
            estorno.setProduto(produto);
            estorno.setTipo(TipoMovimentacao.ENTRADA);
            estorno.setMotivo(MotivoMovimentacaoProduto.AJUSTE);
            estorno.setQuantidade(item.getQuantidade());
            estorno.setObservacao("Estorno da venda #" + venda.getId() + " (excluída)");
            movimentacaoProdutoRepository.save(estorno);
        }

        lancamentoFinanceiroRepository.deleteAll(
                lancamentoFinanceiroRepository.findAllByOrigemAndOrigemId(OrigemLancamento.VENDA, venda.getId()));

        vendaRepository.delete(venda);
    }

    private Cliente buscarCliente(Long clienteId) {
        if (clienteId == null) {
            return null;
        }
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado: " + clienteId));
    }

    private CanalVenda buscarCanal(Long canalId) {
        if (canalId == null) {
            return null;
        }
        return canalVendaRepository.findById(canalId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Canal de venda não encontrado: " + canalId));
    }
}
