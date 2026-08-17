package br.com.artecriativa.api.estoque.dto;

import br.com.artecriativa.api.estoque.MateriaPrima;

import java.math.BigDecimal;
import java.time.Instant;

public record MateriaPrimaResponse(
        Long id,
        String nome,
        String unidadeMedida,
        BigDecimal custoUnitario,
        BigDecimal estoqueAtual,
        BigDecimal estoqueMinimo,
        BigDecimal volumeMl,
        String fornecedor,
        Instant criadoEm,
        Instant atualizadoEm
) {
    public static MateriaPrimaResponse de(MateriaPrima materiaPrima) {
        return new MateriaPrimaResponse(
                materiaPrima.getId(),
                materiaPrima.getNome(),
                materiaPrima.getUnidadeMedida(),
                materiaPrima.getCustoUnitario(),
                materiaPrima.getEstoqueAtual(),
                materiaPrima.getEstoqueMinimo(),
                materiaPrima.getVolumeMl(),
                materiaPrima.getFornecedor(),
                materiaPrima.getCriadoEm(),
                materiaPrima.getAtualizadoEm()
        );
    }
}
