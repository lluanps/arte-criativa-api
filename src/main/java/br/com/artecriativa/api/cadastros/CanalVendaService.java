package br.com.artecriativa.api.cadastros;

import br.com.artecriativa.api.cadastros.dto.CanalVendaRequest;
import br.com.artecriativa.api.common.RecursoNaoEncontradoException;
import br.com.artecriativa.api.vendas.VendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CanalVendaService {

    private final CanalVendaRepository canalVendaRepository;
    private final VendaRepository vendaRepository;

    @Transactional(readOnly = true)
    public List<CanalVenda> listarTodos() {
        return canalVendaRepository.findAllByOrderByNome();
    }

    @Transactional(readOnly = true)
    public CanalVenda buscarPorId(Long id) {
        return canalVendaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Canal de venda não encontrado: " + id));
    }

    @Transactional
    public CanalVenda criar(CanalVendaRequest request) {
        String nome = request.nome().trim();
        if (canalVendaRepository.existsByNomeIgnoreCase(nome)) {
            throw new IllegalStateException("Já existe um canal de venda com esse nome");
        }
        CanalVenda canalVenda = new CanalVenda();
        canalVenda.setNome(nome);
        return canalVendaRepository.save(canalVenda);
    }

    @Transactional
    public CanalVenda atualizar(Long id, CanalVendaRequest request) {
        CanalVenda canalVenda = buscarPorId(id);
        String nome = request.nome().trim();
        if (!nome.equalsIgnoreCase(canalVenda.getNome()) && canalVendaRepository.existsByNomeIgnoreCase(nome)) {
            throw new IllegalStateException("Já existe um canal de venda com esse nome");
        }
        canalVenda.setNome(nome);
        return canalVendaRepository.save(canalVenda);
    }

    @Transactional
    public void excluir(Long id) {
        CanalVenda canalVenda = buscarPorId(id);
        long vendas = vendaRepository.countByCanalId(id);
        if (vendas > 0) {
            throw new IllegalStateException(
                    "Não é possível excluir '%s': %s vinculada(s) a este canal."
                            .formatted(canalVenda.getNome(), vendas == 1 ? "1 venda está" : vendas + " vendas estão"));
        }
        canalVendaRepository.delete(canalVenda);
    }
}
