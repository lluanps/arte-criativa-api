package br.com.artecriativa.api.estoque.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

/**
 * Editar uma matéria-prima já cadastrada — só metadados (nome, unidade, estoque
 * mínimo, volume, fornecedor). Custo unitário e estoque atual não entram aqui de
 * propósito: só mudam via {@code MovimentacaoMateriaPrimaRequest} (Registrar
 * movimentação), pra sempre deixar rastro no Financeiro quando o custo muda de
 * verdade — editar "na mão" era a outra porta aberta pra um custo nunca virar despesa
 * nem ficar rastreado.
 */
public record MateriaPrimaAtualizacaoRequest(
        @NotBlank(message = "nome é obrigatório") String nome,
        Long categoriaId,
        @NotBlank(message = "unidade de medida é obrigatória") String unidadeMedida,
        @DecimalMin(value = "0.0", message = "estoque mínimo não pode ser negativo") BigDecimal estoqueMinimo,
        String fornecedor
) {
    public MateriaPrimaAtualizacaoRequest {
        if (estoqueMinimo == null) {
            estoqueMinimo = BigDecimal.ZERO;
        }
    }
}
