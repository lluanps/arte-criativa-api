package br.com.artecriativa.api.estoque.dto;

import br.com.artecriativa.api.estoque.MateriaPrima;

import java.math.BigDecimal;
import java.time.Instant;

public record MateriaPrimaResponse(
        Long id,
        String nome,
        Long categoriaId,
        String categoriaNome,
        String unidadeMedida,
        BigDecimal custoUnitario,
        BigDecimal estoqueAtual,
        BigDecimal estoqueMinimo,
        String fornecedor,
        Instant criadoEm,
        Instant atualizadoEm
) {
    public static MateriaPrimaResponse de(MateriaPrima materiaPrima) {
        return new MateriaPrimaResponse(
                materiaPrima.getId(),
                materiaPrima.getNome(),
                materiaPrima.getCategoria() != null ? materiaPrima.getCategoria().getId() : null,
                materiaPrima.getCategoria() != null ? materiaPrima.getCategoria().getNome() : null,
                materiaPrima.getUnidadeMedida(),
                materiaPrima.getCustoUnitario(),
                materiaPrima.getEstoqueAtual(),
                materiaPrima.getEstoqueMinimo(),
                materiaPrima.getFornecedor(),
                materiaPrima.getCriadoEm(),
                materiaPrima.getAtualizadoEm()
        );
    }
}
