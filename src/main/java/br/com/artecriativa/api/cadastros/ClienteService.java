package br.com.artecriativa.api.cadastros;

import br.com.artecriativa.api.cadastros.dto.ClienteRequest;
import br.com.artecriativa.api.common.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Transactional(readOnly = true)
    public List<Cliente> listarTodos() {
        return clienteRepository.findAllByOrderByNome();
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado: " + id));
    }

    @Transactional
    public Cliente criar(ClienteRequest request) {
        Cliente cliente = new Cliente();
        aplicarRequest(cliente, request);
        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente atualizar(Long id, ClienteRequest request) {
        Cliente cliente = buscarPorId(id);
        aplicarRequest(cliente, request);
        return clienteRepository.save(cliente);
    }

    @Transactional
    public void excluir(Long id) {
        Cliente cliente = buscarPorId(id);
        clienteRepository.delete(cliente);
    }

    private void aplicarRequest(Cliente cliente, ClienteRequest request) {
        cliente.setNome(request.nome().trim());
        cliente.setTelefone(request.telefone());
        cliente.setEmail(request.email());
    }
}
