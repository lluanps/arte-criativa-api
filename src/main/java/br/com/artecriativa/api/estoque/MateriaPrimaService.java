package br.com.artecriativa.api.estoque;

import br.com.artecriativa.api.common.RecursoNaoEncontradoException;
import br.com.artecriativa.api.estoque.dto.MateriaPrimaRequest;
import br.com.artecriativa.api.estoque.dto.MovimentacaoMateriaPrimaRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MateriaPrimaService {

    private final MateriaPrimaRepository materiaPrimaRepository;
    private final MovimentacaoMateriaPrimaRepository movimentacaoRepository;

    public List<MateriaPrima> listarTodas() {
        return materiaPrimaRepository.findAll();
    }

    public MateriaPrima buscarPorId(Long id) {
        return materiaPrimaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Matéria-prima não encontrada: " + id));
    }

    @Transactional
    public MateriaPrima criar(MateriaPrimaRequest request) {
        MateriaPrima materiaPrima = new MateriaPrima();
        aplicarRequest(materiaPrima, request);
        return materiaPrimaRepository.save(materiaPrima);
    }

    @Transactional
    public MateriaPrima atualizar(Long id, MateriaPrimaRequest request) {
        MateriaPrima materiaPrima = buscarPorId(id);
        aplicarRequest(materiaPrima, request);
        return materiaPrimaRepository.save(materiaPrima);
    }

    @Transactional
    public void excluir(Long id) {
        MateriaPrima materiaPrima = buscarPorId(id);
        materiaPrimaRepository.delete(materiaPrima);
    }

    /**
     * Registra uma entrada (compra) ou saída (consumo/perda) de matéria-prima
     * e atualiza o saldo atual. Impede que o saldo fique negativo.
     */
    @Transactional
    public MovimentacaoMateriaPrima registrarMovimentacao(Long materiaPrimaId, MovimentacaoMateriaPrimaRequest request) {
        MateriaPrima materiaPrima = buscarPorId(materiaPrimaId);

        BigDecimal novoEstoque = switch (request.tipo()) {
            case ENTRADA -> materiaPrima.getEstoqueAtual().add(request.quantidade());
            case SAIDA -> materiaPrima.getEstoqueAtual().subtract(request.quantidade());
        };

        if (novoEstoque.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException(
                    "Estoque insuficiente da matéria-prima '%s'. Disponível: %s, solicitado: %s"
                            .formatted(materiaPrima.getNome(), materiaPrima.getEstoqueAtual(), request.quantidade()));
        }

        MovimentacaoMateriaPrima movimentacao = new MovimentacaoMateriaPrima();
        movimentacao.setMateriaPrima(materiaPrima);
        movimentacao.setTipo(request.tipo());
        movimentacao.setMotivo(request.motivo());
        movimentacao.setQuantidade(request.quantidade());
        movimentacao.setObservacao(request.observacao());

        materiaPrima.setEstoqueAtual(novoEstoque);
        materiaPrimaRepository.save(materiaPrima);

        return movimentacaoRepository.save(movimentacao);
    }

    public List<MovimentacaoMateriaPrima> listarMovimentacoes(Long materiaPrimaId) {
        buscarPorId(materiaPrimaId);
        return movimentacaoRepository.findByMateriaPrimaIdOrderByDataMovimentacaoDesc(materiaPrimaId);
    }

    private void aplicarRequest(MateriaPrima materiaPrima, MateriaPrimaRequest request) {
        materiaPrima.setNome(request.nome());
        materiaPrima.setUnidadeMedida(request.unidadeMedida());
        materiaPrima.setCustoUnitario(request.custoUnitario());
        materiaPrima.setEstoqueMinimo(request.estoqueMinimo());
        materiaPrima.setFornecedor(request.fornecedor());
    }
}
