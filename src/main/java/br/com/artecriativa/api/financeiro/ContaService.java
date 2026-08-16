package br.com.artecriativa.api.financeiro;

import br.com.artecriativa.api.common.RecursoNaoEncontradoException;
import br.com.artecriativa.api.financeiro.dto.ContaRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContaService {

    private final ContaRepository contaRepository;

    @Transactional(readOnly = true)
    public List<Conta> listarTodas() {
        return contaRepository.findAllByOrderByVencimento();
    }

    @Transactional(readOnly = true)
    public List<Conta> listarPorTipo(TipoConta tipo) {
        return contaRepository.findByTipoOrderByVencimento(tipo);
    }

    @Transactional(readOnly = true)
    public Conta buscarPorId(Long id) {
        return contaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não encontrada: " + id));
    }

    @Transactional
    public Conta criar(ContaRequest request) {
        Conta conta = new Conta();
        aplicarRequest(conta, request);
        return contaRepository.save(conta);
    }

    @Transactional
    public Conta atualizar(Long id, ContaRequest request) {
        Conta conta = buscarPorId(id);
        aplicarRequest(conta, request);
        return contaRepository.save(conta);
    }

    @Transactional
    public Conta marcarComoPaga(Long id) {
        Conta conta = buscarPorId(id);
        if (conta.getStatus() == StatusConta.PAGO) {
            throw new IllegalStateException("Conta #%d já está paga".formatted(id));
        }
        conta.setStatus(StatusConta.PAGO);
        conta.setPagoEm(Instant.now());
        return contaRepository.save(conta);
    }

    @Transactional
    public void excluir(Long id) {
        Conta conta = buscarPorId(id);
        contaRepository.delete(conta);
    }

    private void aplicarRequest(Conta conta, ContaRequest request) {
        conta.setTipo(request.tipo());
        conta.setDescricao(request.descricao());
        conta.setValor(request.valor());
        conta.setVencimento(request.vencimento());
    }
}
