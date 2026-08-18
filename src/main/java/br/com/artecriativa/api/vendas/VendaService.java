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

        Venda venda = new Venda();
        venda.setCliente(cliente);
        venda.setCanal(canal);
        venda.setValorTotal(valorTotal);
        venda.adicionarItens(itens);
        venda = vendaRepository.save(venda);

        LancamentoFinanceiro lancamento = new LancamentoFinanceiro();
        lancamento.setTipo(TipoLancamento.RECEITA);
        lancamento.setCategoria("Venda");
        lancamento.setValor(valorTotal);
        lancamento.setDescricao("Venda #" + venda.getId() + (cliente != null ? " - " + cliente.getNome() : ""));
        lancamento.setOrigem(OrigemLancamento.VENDA);
        lancamento.setOrigemId(venda.getId());
        lancamentoFinanceiroRepository.save(lancamento);

        return venda;
    }

    /**
     * Exclui a venda estornando os efeitos colaterais do registro original: devolve a
     * quantidade de cada item ao estoque (com uma movimentação de AJUSTE explicando o
     * estorno) e remove o lançamento financeiro de receita gerado por ela. Sem isso, o
     * estoque e o financeiro ficariam desalinhados com a realidade.
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

        lancamentoFinanceiroRepository.findByOrigemAndOrigemId(OrigemLancamento.VENDA, venda.getId())
                .ifPresent(lancamentoFinanceiroRepository::delete);

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
