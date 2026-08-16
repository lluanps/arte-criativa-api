package br.com.artecriativa.api.financeiro;

import br.com.artecriativa.api.common.RecursoNaoEncontradoException;
import br.com.artecriativa.api.financeiro.dto.LancamentoFinanceiroRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * CRUD de lançamentos financeiros manuais. Lançamentos gerados automaticamente por
 * outro módulo (ex: {@code origem = VENDA}) não podem ser editados nem excluídos por
 * aqui — o registro precisa ser ajustado na origem, senão o vínculo perde sentido.
 */
@Service
@RequiredArgsConstructor
public class LancamentoFinanceiroService {

    private final LancamentoFinanceiroRepository lancamentoFinanceiroRepository;

    @Transactional(readOnly = true)
    public List<LancamentoFinanceiro> listarTodos() {
        return lancamentoFinanceiroRepository.findAllByOrderByDataLancamentoDesc();
    }

    @Transactional(readOnly = true)
    public List<LancamentoFinanceiro> listarPorPeriodo(LocalDate inicio, LocalDate fim) {
        return lancamentoFinanceiroRepository.findByDataLancamentoBetweenOrderByDataLancamentoDesc(inicio, fim);
    }

    @Transactional(readOnly = true)
    public LancamentoFinanceiro buscarPorId(Long id) {
        return lancamentoFinanceiroRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lançamento financeiro não encontrado: " + id));
    }

    @Transactional
    public LancamentoFinanceiro criar(LancamentoFinanceiroRequest request) {
        LancamentoFinanceiro lancamento = new LancamentoFinanceiro();
        lancamento.setOrigem(OrigemLancamento.MANUAL);
        aplicarRequest(lancamento, request);
        return lancamentoFinanceiroRepository.save(lancamento);
    }

    @Transactional
    public LancamentoFinanceiro atualizar(Long id, LancamentoFinanceiroRequest request) {
        LancamentoFinanceiro lancamento = buscarPorId(id);
        exigirOrigemManual(lancamento);
        aplicarRequest(lancamento, request);
        return lancamentoFinanceiroRepository.save(lancamento);
    }

    @Transactional
    public void excluir(Long id) {
        LancamentoFinanceiro lancamento = buscarPorId(id);
        exigirOrigemManual(lancamento);
        lancamentoFinanceiroRepository.delete(lancamento);
    }

    private void exigirOrigemManual(LancamentoFinanceiro lancamento) {
        if (lancamento.getOrigem() != OrigemLancamento.MANUAL) {
            throw new IllegalStateException(
                    "Lançamento gerado automaticamente por %s (#%d) não pode ser editado ou excluído diretamente"
                            .formatted(lancamento.getOrigem(), lancamento.getOrigemId()));
        }
    }

    private void aplicarRequest(LancamentoFinanceiro lancamento, LancamentoFinanceiroRequest request) {
        lancamento.setTipo(request.tipo());
        lancamento.setCategoria(request.categoria());
        lancamento.setValor(request.valor());
        lancamento.setDescricao(request.descricao());
        lancamento.setDataLancamento(request.dataLancamento());
    }
}
