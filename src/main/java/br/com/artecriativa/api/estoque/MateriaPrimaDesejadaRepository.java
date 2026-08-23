package br.com.artecriativa.api.estoque;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MateriaPrimaDesejadaRepository extends JpaRepository<MateriaPrimaDesejada, Long> {
    List<MateriaPrimaDesejada> findAllByOrderByNomeAsc();
}
