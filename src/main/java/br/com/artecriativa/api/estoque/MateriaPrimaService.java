package br.com.artecriativa.api.estoque;

import br.com.artecriativa.api.cadastros.CategoriaMateriaPrima;
import br.com.artecriativa.api.cadastros.CategoriaMateriaPrimaRepository;
import br.com.artecriativa.api.cadastros.Fornecedor;
import br.com.artecriativa.api.cadastros.FornecedorRepository;
import br.com.artecriativa.api.common.FormatoNumerico;
import br.com.artecriativa.api.common.MensagemVinculo;
import br.com.artecriativa.api.common.PaginaResponse;
import br.com.artecriativa.api.common.RecursoNaoEncontradoException;
import br.com.artecriativa.api.estoque.dto.MateriaPrimaAtualizacaoRequest;
import br.com.artecriativa.api.estoque.dto.MateriaPrimaRequest;
import br.com.artecriativa.api.estoque.dto.MateriaPrimaResponse;
import br.com.artecriativa.api.estoque.dto.MovimentacaoMateriaPrimaRequest;
import br.com.artecriativa.api.common.ConflitoOperacaoException;
import br.com.artecriativa.api.financeiro.LancamentoFinanceiro;
import br.com.artecriativa.api.financeiro.LancamentoFinanceiroRepository;
import br.com.artecriativa.api.financeiro.OrigemLancamento;
import br.com.artecriativa.api.financeiro.TipoLancamento;
import br.com.artecriativa.api.financeiro.dto.ItemMateriaPrimaCompraResponse;
import br.com.artecriativa.api.producao.ReceitaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MateriaPrimaService {

    private final MateriaPrimaRepository materiaPrimaRepository;
    private final MovimentacaoMateriaPrimaRepository movimentacaoRepository;
    private final ReceitaRepository receitaRepository;
    private final LancamentoFinanceiroRepository lancamentoFinanceiroRepository;
    private final CategoriaMateriaPrimaRepository categoriaRepository;
    private final FornecedorRepository fornecedorRepository;

    private static final Map<String, String> CAMPOS_ORDENACAO = Map.of(
            "nome", "nome",
            "unidadeMedida", "unidadeMedida",
            "custoUnitario", "custoUnitario",
            "estoqueAtual", "estoqueAtual");

    public List<MateriaPrima> listarTodas() {
        return materiaPrimaRepository.findAll();
    }

    /** Busca paginada com filtros pra tela de listagem (ver {@link MateriaPrimaRepository#buscar}). */
    public PaginaResponse<MateriaPrimaResponse> buscarPaginado(String busca, Long categoriaId, boolean estoqueBaixo,
                                                                 int pagina, int tamanho, String ordenarPor, String direcao) {
        String buscaNormalizada = (busca == null || busca.isBlank()) ? "" : busca.trim();
        String campo = CAMPOS_ORDENACAO.getOrDefault(ordenarPor, "nome");
        Sort.Direction dir = "desc".equalsIgnoreCase(direcao) ? Sort.Direction.DESC : Sort.Direction.ASC;
        int tamanhoValido = Math.min(Math.max(tamanho, 1), 100);
        Pageable pageable = PageRequest.of(Math.max(pagina, 0), tamanhoValido, Sort.by(dir, campo));

        return PaginaResponse.de(
                materiaPrimaRepository.buscar(buscaNormalizada, categoriaId, estoqueBaixo, pageable),
                MateriaPrimaResponse::de);
    }

    public MateriaPrima buscarPorId(Long id) {
        return materiaPrimaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Matéria-prima não encontrada: " + id));
    }

    /**
     * Criar é sempre "registrar a primeira compra" (ver javadoc de
     * {@link MateriaPrimaRequest}) — nasce com custo unitário calculado
     * (valorPago ÷ quantidadeComprada), estoque já preenchido, uma
     * {@link MovimentacaoMateriaPrima} de COMPRA registrada pro histórico, e a
     * despesa correspondente lançada no Financeiro. Mesma lógica de
     * {@link #registrarMovimentacao}, só que aqui a matéria-prima ainda nem existe.
     */
    @Transactional
    public MateriaPrima criar(MateriaPrimaRequest request) {
        MateriaPrima materiaPrima = new MateriaPrima();
        aplicarMetadados(materiaPrima, request.nome(), request.categoriaId(), request.unidadeMedida(),
                request.estoqueMinimo(), request.fornecedorId());

        BigDecimal custoUnitario = request.valorPago().divide(request.quantidadeComprada(), 4, RoundingMode.HALF_UP);
        materiaPrima.setCustoUnitario(custoUnitario);
        materiaPrima.setEstoqueAtual(request.quantidadeComprada());
        materiaPrima = materiaPrimaRepository.save(materiaPrima);

        MovimentacaoMateriaPrima movimentacao = new MovimentacaoMateriaPrima();
        movimentacao.setMateriaPrima(materiaPrima);
        movimentacao.setTipo(TipoMovimentacao.ENTRADA);
        movimentacao.setMotivo(MotivoMovimentacaoMateriaPrima.COMPRA);
        movimentacao.setQuantidade(request.quantidadeComprada());
        movimentacao.setValorPago(request.valorPago());
        movimentacao.setCustoUnitarioApurado(custoUnitario);
        movimentacao.setObservacao("Compra inicial (cadastro da matéria-prima)");
        movimentacao = movimentacaoRepository.save(movimentacao);

        lancarDespesaCompra(materiaPrima, movimentacao, request.quantidadeComprada(), request.valorPago());

        return materiaPrima;
    }

    /** Só metadados — custo unitário e estoque não entram aqui de propósito, ver
     * javadoc de {@link MateriaPrimaAtualizacaoRequest}. */
    @Transactional
    public MateriaPrima atualizar(Long id, MateriaPrimaAtualizacaoRequest request) {
        MateriaPrima materiaPrima = buscarPorId(id);
        aplicarMetadados(materiaPrima, request.nome(), request.categoriaId(), request.unidadeMedida(),
                request.estoqueMinimo(), request.fornecedorId());
        return materiaPrimaRepository.save(materiaPrima);
    }

    @Transactional
    public void excluir(Long id) {
        MateriaPrima materiaPrima = buscarPorId(id);

        List<String> vinculos = new ArrayList<>();
        MensagemVinculo.add(vinculos, movimentacaoRepository.countByMateriaPrimaId(id),
                "movimentação de estoque", "movimentações de estoque");
        MensagemVinculo.add(vinculos, receitaRepository.countByItens_MateriaPrimaId(id),
                "ficha técnica", "fichas técnicas");
        if (!vinculos.isEmpty()) {
            throw new IllegalStateException(
                    "Não é possível excluir '%s': existem %s vinculados a esta matéria-prima."
                            .formatted(materiaPrima.getNome(), MensagemVinculo.juntarComE(vinculos)));
        }

        materiaPrimaRepository.delete(materiaPrima);
    }

    /**
     * Registra uma entrada (compra) ou saída (consumo/perda) de matéria-prima
     * e atualiza o saldo atual. Impede que o saldo fique negativo.
     */
    @Transactional
    public MovimentacaoMateriaPrima registrarMovimentacao(Long materiaPrimaId, MovimentacaoMateriaPrimaRequest request) {
        MateriaPrima materiaPrima = buscarPorId(materiaPrimaId);
        BigDecimal estoqueAntes = materiaPrima.getEstoqueAtual();

        BigDecimal novoEstoque = switch (request.tipo()) {
            case ENTRADA -> estoqueAntes.add(request.quantidade());
            case SAIDA -> estoqueAntes.subtract(request.quantidade());
        };

        if (novoEstoque.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException(
                    "Estoque insuficiente da matéria-prima '%s'. Disponível: %s, solicitado: %s"
                            .formatted(materiaPrima.getNome(),
                                    FormatoNumerico.semZerosDesnecessarios(materiaPrima.getEstoqueAtual()),
                                    FormatoNumerico.semZerosDesnecessarios(request.quantidade())));
        }

        BigDecimal custoUnitarioApurado = null;
        if (request.valorPago() != null) {
            if (request.tipo() != TipoMovimentacao.ENTRADA) {
                throw new IllegalStateException("Valor pago só se aplica a uma entrada (compra) de matéria-prima.");
            }
            custoUnitarioApurado = request.valorPago().divide(request.quantidade(), 4, RoundingMode.HALF_UP);
            materiaPrima.setCustoUnitario(
                    custoMedioPonderado(estoqueAntes, materiaPrima.getCustoUnitario(), request.quantidade(), custoUnitarioApurado));
        }

        MovimentacaoMateriaPrima movimentacao = new MovimentacaoMateriaPrima();
        movimentacao.setMateriaPrima(materiaPrima);
        movimentacao.setTipo(request.tipo());
        movimentacao.setMotivo(request.motivo());
        movimentacao.setQuantidade(request.quantidade());
        movimentacao.setValorPago(request.valorPago());
        movimentacao.setCustoUnitarioApurado(custoUnitarioApurado);
        movimentacao.setObservacao(request.observacao());

        materiaPrima.setEstoqueAtual(novoEstoque);
        materiaPrimaRepository.save(materiaPrima);
        movimentacao = movimentacaoRepository.save(movimentacao);

        // Só lança despesa quando teve valor pago de verdade (uma compra) — entrada de
        // AJUSTE/PRODUCAO sem valorPago não representa dinheiro saindo do caixa.
        if (request.valorPago() != null) {
            lancarDespesaCompra(materiaPrima, movimentacao, request.quantidade(), request.valorPago());
        }

        return movimentacao;
    }

    /**
     * Registra a entrada de uma compra de matéria-prima que já nasceu vinculada a uma
     * {@link br.com.artecriativa.api.financeiro.Conta} (ver {@code ContaService}) — mesma
     * lógica de estoque/custo médio ponderado de {@link #registrarMovimentacao}, motivo
     * COMPRA, mas <b>sem</b> chamar {@link #lancarDespesaCompra}: a despesa nasce só
     * quando a conta é paga (via {@code ContaService#sincronizarLancamento}), senão a
     * mesma compra apareceria duas vezes no Financeiro. Exatamente um entre
     * {@code contaId}/{@code grupoParcelamentoId} deve vir preenchido — o outro nulo.
     */
    @Transactional
    public void registrarEntradaVinculadaAConta(Long materiaPrimaId, BigDecimal quantidade, BigDecimal valor,
                                                  Long contaId, UUID grupoParcelamentoId) {
        MateriaPrima materiaPrima = buscarPorId(materiaPrimaId);
        BigDecimal estoqueAntes = materiaPrima.getEstoqueAtual();
        BigDecimal custoUnitarioApurado = valor.divide(quantidade, 4, RoundingMode.HALF_UP);
        materiaPrima.setCustoUnitario(
                custoMedioPonderado(estoqueAntes, materiaPrima.getCustoUnitario(), quantidade, custoUnitarioApurado));
        materiaPrima.setEstoqueAtual(estoqueAntes.add(quantidade));
        materiaPrimaRepository.save(materiaPrima);

        MovimentacaoMateriaPrima movimentacao = new MovimentacaoMateriaPrima();
        movimentacao.setMateriaPrima(materiaPrima);
        movimentacao.setTipo(TipoMovimentacao.ENTRADA);
        movimentacao.setMotivo(MotivoMovimentacaoMateriaPrima.COMPRA);
        movimentacao.setQuantidade(quantidade);
        movimentacao.setValorPago(valor);
        movimentacao.setCustoUnitarioApurado(custoUnitarioApurado);
        movimentacao.setContaId(contaId);
        movimentacao.setGrupoParcelamentoId(grupoParcelamentoId);
        movimentacao.setObservacao("Compra vinculada à conta"
                + (grupoParcelamentoId != null ? " parcelada" : " #" + contaId));
        movimentacaoRepository.save(movimentacao);
    }

    /** Compras de matéria-prima vinculadas a esta conta/grupo (ver
     * {@link #registrarEntradaVinculadaAConta}) — usado pra montar
     * {@code ContaResponse#itensMateriaPrima}. Vazio pra qualquer conta comum. */
    public List<ItemMateriaPrimaCompraResponse> buscarItensDeConta(Long contaId, UUID grupoParcelamentoId) {
        List<MovimentacaoMateriaPrima> movimentacoes = grupoParcelamentoId != null
                ? movimentacaoRepository.findByGrupoParcelamentoId(grupoParcelamentoId)
                : movimentacaoRepository.findByContaId(contaId);
        return movimentacoes.stream()
                .map(m -> new ItemMateriaPrimaCompraResponse(
                        m.getMateriaPrima().getId(), m.getMateriaPrima().getNome(), m.getQuantidade(), m.getValorPago()))
                .toList();
    }

    /** Se essa conta/grupo tem alguma compra de matéria-prima vinculada — usado pra
     * bloquear edição de valor em {@code ContaService#atualizar}. */
    public boolean existeCompraVinculada(Long contaId, UUID grupoParcelamentoId) {
        return grupoParcelamentoId != null
                ? movimentacaoRepository.existsByGrupoParcelamentoId(grupoParcelamentoId)
                : movimentacaoRepository.existsByContaId(contaId);
    }

    /** Desfaz (decrementa o estoque e reverte o custo unitário médio de) todas as
     * compras vinculadas a uma conta AVULSA — chamado por {@code ContaService#excluir}. */
    @Transactional
    public void estornarComprasVinculadasAConta(Long contaId) {
        estornarMovimentacoes(movimentacaoRepository.findByContaId(contaId));
    }

    /** Mesma ideia de {@link #estornarComprasVinculadasAConta}, pro grupo inteiro de uma
     * conta parcelada — chamado só quando a última parcela do grupo está sendo excluída. */
    @Transactional
    public void estornarComprasVinculadasAGrupo(UUID grupoParcelamentoId) {
        estornarMovimentacoes(movimentacaoRepository.findByGrupoParcelamentoId(grupoParcelamentoId));
    }

    /**
     * Reverte estoque e custo unitário médio das movimentações informadas — só quando
     * seguro: cada uma tem que ser, no momento da exclusão, a movimentação MAIS RECENTE
     * daquela matéria-prima. Se algo mais aconteceu depois (qualquer tipo de
     * movimentação), o custo médio ponderado já foi "misturado" com esse evento
     * seguinte e não dá pra desfazer com precisão — bloqueia nesse caso em vez de
     * reverter parcialmente/errado (mesmo padrão conservador do bloqueio de estoque
     * negativo logo abaixo).
     */
    private void estornarMovimentacoes(List<MovimentacaoMateriaPrima> movimentacoes) {
        if (movimentacoes.isEmpty()) {
            return;
        }

        // Valida tudo antes de aplicar qualquer estorno, pra não deixar a exclusão pela
        // metade. maiorIdNoLote ignora as próprias movimentações deste estorno na hora
        // de checar "aconteceu algo depois" — senão uma conta com 2 itens da mesma
        // matéria-prima se bloquearia sozinha.
        Map<Long, Long> maiorIdNoLotePorMateriaPrima = new HashMap<>();
        for (MovimentacaoMateriaPrima movimentacao : movimentacoes) {
            maiorIdNoLotePorMateriaPrima.merge(movimentacao.getMateriaPrima().getId(), movimentacao.getId(), Math::max);
        }
        for (MovimentacaoMateriaPrima movimentacao : movimentacoes) {
            MateriaPrima materiaPrima = movimentacao.getMateriaPrima();
            Long maiorIdNoLote = maiorIdNoLotePorMateriaPrima.get(materiaPrima.getId());
            if (movimentacaoRepository.existsByMateriaPrimaIdAndIdGreaterThan(materiaPrima.getId(), maiorIdNoLote)) {
                throw new ConflitoOperacaoException(
                        ("Não é possível excluir: já houve outra movimentação de '%s' depois desta compra — "
                                + "o custo unitário médio não pode ser revertido com segurança nesse caso.")
                                        .formatted(materiaPrima.getNome()));
            }
            if (materiaPrima.getEstoqueAtual().compareTo(movimentacao.getQuantidade()) < 0) {
                throw new ConflitoOperacaoException(
                        ("Não é possível excluir: estornar essa compra deixaria o estoque de '%s' negativo "
                                + "(disponível: %s, seria estornado: %s). Ajuste o estoque manualmente antes de excluir.")
                                        .formatted(materiaPrima.getNome(),
                                                FormatoNumerico.semZerosDesnecessarios(materiaPrima.getEstoqueAtual()),
                                                FormatoNumerico.semZerosDesnecessarios(movimentacao.getQuantidade())));
            }
        }

        // Desfaz da mais nova pra mais velha -- importante se a mesma matéria-prima
        // aparecer mais de uma vez neste mesmo estorno, senão a segunda reversão usaria
        // um custo unitário já parcialmente revertido como "atual".
        List<MovimentacaoMateriaPrima> emOrdemReversa = movimentacoes.stream()
                .sorted(Comparator.comparing(MovimentacaoMateriaPrima::getId).reversed())
                .toList();
        for (MovimentacaoMateriaPrima movimentacao : emOrdemReversa) {
            MateriaPrima materiaPrima = movimentacao.getMateriaPrima();
            BigDecimal estoqueDepois = materiaPrima.getEstoqueAtual();
            BigDecimal estoqueAntes = estoqueDepois.subtract(movimentacao.getQuantidade());
            materiaPrima.setEstoqueAtual(estoqueAntes);

            if (estoqueAntes.compareTo(BigDecimal.ZERO) == 0) {
                // Não tinha nada antes desta entrada (era a primeira) -- com estoque 0 o
                // custo médio anterior tinha peso zero na fórmula, então não tem "antes"
                // pra recuperar; zera, coerente com o padrão da entidade.
                materiaPrima.setCustoUnitario(BigDecimal.ZERO);
            } else if (movimentacao.getCustoUnitarioApurado() != null) {
                // Inverte exatamente a média ponderada aplicada na hora da entrada --
                // seguro porque acabamos de garantir (validação acima) que nada mudou
                // nesta matéria-prima desde então.
                BigDecimal custoAntes = materiaPrima.getCustoUnitario().multiply(estoqueDepois)
                        .subtract(movimentacao.getQuantidade().multiply(movimentacao.getCustoUnitarioApurado()))
                        .divide(estoqueAntes, 4, RoundingMode.HALF_UP);
                materiaPrima.setCustoUnitario(custoAntes);
            }

            materiaPrimaRepository.save(materiaPrima);
            movimentacaoRepository.delete(movimentacao);
        }
    }

    /** Compartilhado por {@link #criar} (primeira compra) e {@link #registrarMovimentacao}
     * (compras seguintes) — mesmo formato de despesa nos dois casos. */
    private void lancarDespesaCompra(MateriaPrima materiaPrima, MovimentacaoMateriaPrima movimentacao,
                                       BigDecimal quantidade, BigDecimal valorPago) {
        LancamentoFinanceiro lancamento = new LancamentoFinanceiro();
        lancamento.setTipo(TipoLancamento.DESPESA);
        lancamento.setCategoria("Compra de matéria-prima");
        lancamento.setValor(valorPago);
        lancamento.setDescricao("Compra de %s %s de %s".formatted(
                FormatoNumerico.semZerosDesnecessarios(quantidade),
                materiaPrima.getUnidadeMedida(),
                materiaPrima.getNome()));
        lancamento.setOrigem(OrigemLancamento.COMPRA);
        lancamento.setOrigemId(movimentacao.getId());
        lancamentoFinanceiroRepository.save(lancamento);
    }

    /**
     * Custo médio ponderado entre o que já tinha em estoque (a um custo) e o que acabou
     * de entrar (a outro custo) — evita que o custo da ficha técnica pule bruscamente a
     * cada compra feita a um preço diferente do anterior.
     */
    private static BigDecimal custoMedioPonderado(BigDecimal qtdAntiga, BigDecimal custoAntigo,
                                                    BigDecimal qtdNova, BigDecimal custoNovo) {
        BigDecimal qtdTotal = qtdAntiga.add(qtdNova);
        if (qtdTotal.compareTo(BigDecimal.ZERO) == 0) {
            return custoNovo;
        }
        BigDecimal valorTotal = qtdAntiga.multiply(custoAntigo).add(qtdNova.multiply(custoNovo));
        return valorTotal.divide(qtdTotal, 4, RoundingMode.HALF_UP);
    }

    public List<MovimentacaoMateriaPrima> listarMovimentacoes(Long materiaPrimaId) {
        buscarPorId(materiaPrimaId);
        return movimentacaoRepository.findByMateriaPrimaIdOrderByDataMovimentacaoDesc(materiaPrimaId);
    }

    private void aplicarMetadados(MateriaPrima materiaPrima, String nome, Long categoriaId, String unidadeMedida,
                                    BigDecimal estoqueMinimo, Long fornecedorId) {
        materiaPrima.setNome(nome);
        materiaPrima.setCategoria(buscarCategoria(categoriaId));
        materiaPrima.setUnidadeMedida(unidadeMedida);
        materiaPrima.setEstoqueMinimo(estoqueMinimo);
        materiaPrima.setFornecedor(buscarFornecedor(fornecedorId));
    }

    private CategoriaMateriaPrima buscarCategoria(Long categoriaId) {
        if (categoriaId == null) {
            return null;
        }
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria de matéria-prima não encontrada: " + categoriaId));
    }

    private Fornecedor buscarFornecedor(Long fornecedorId) {
        if (fornecedorId == null) {
            return null;
        }
        return fornecedorRepository.findById(fornecedorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Fornecedor não encontrado: " + fornecedorId));
    }
}
