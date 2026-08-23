package br.com.artecriativa.api.estoque;

import br.com.artecriativa.api.common.RecursoNaoEncontradoException;
import br.com.artecriativa.api.estoque.dto.MateriaPrimaDesejadaRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MateriaPrimaDesejadaService {

    private final MateriaPrimaDesejadaRepository repository;

    public List<MateriaPrimaDesejada> listarTodas() {
        return repository.findAllByOrderByNomeAsc();
    }

    @Transactional
    public MateriaPrimaDesejada criar(MateriaPrimaDesejadaRequest request) {
        MateriaPrimaDesejada desejada = new MateriaPrimaDesejada();
        desejada.setNome(request.nome());
        return repository.save(desejada);
    }

    @Transactional
    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Item da lista de compras não encontrado: " + id);
        }
        repository.deleteById(id);
    }
}
