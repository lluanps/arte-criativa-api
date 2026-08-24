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
        Long fornecedorId,
        String fornecedorNome,
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
                materiaPrima.getFornecedor() != null ? materiaPrima.getFornecedor().getId() : null,
                materiaPrima.getFornecedor() != null ? materiaPrima.getFornecedor().getNome() : null,
                materiaPrima.getCriadoEm(),
                materiaPrima.getAtualizadoEm()
        );
    }
}
