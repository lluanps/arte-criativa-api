package br.com.artecriativa.api.estoque.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Criar uma matéria-prima é sempre "registrar a primeira compra" — não dá pra digitar
 * um custo unitário direto (evitava um valor inventado entrando sem querer no custo de
 * uma ficha técnica, e sem gerar despesa nenhuma no Financeiro). O custo unitário vem
 * de {@code valorPago ÷ quantidadeComprada}, do mesmo jeito que
 * {@link MovimentacaoMateriaPrimaRequest} já faz pras compras seguintes.
 * <p>
 * Pra só anotar um nome sem saber o preço ainda ("vou comprar uma essência nova, mas
 * não sei o preço"), usar {@code MateriaPrimaDesejadaRequest} em vez deste — uma
 * matéria-prima "de verdade" só existe quando a compra é registrada.
 */
public record MateriaPrimaRequest(
        @NotBlank(message = "nome é obrigatório") String nome,
        @NotBlank(message = "unidade de medida é obrigatória") String unidadeMedida,
        @NotNull(message = "quantidade comprada é obrigatória")
        @DecimalMin(value = "0.0", inclusive = false, message = "quantidade comprada deve ser maior que zero") BigDecimal quantidadeComprada,
        @NotNull(message = "valor pago é obrigatório")
        @DecimalMin(value = "0.0", inclusive = false, message = "valor pago deve ser maior que zero") BigDecimal valorPago,
        @DecimalMin(value = "0.0", message = "estoque mínimo não pode ser negativo") BigDecimal estoqueMinimo,
        @DecimalMin(value = "0.0", message = "volume não pode ser negativo") BigDecimal volumeMl,
        String fornecedor
) {
    public MateriaPrimaRequest {
        if (estoqueMinimo == null) {
            estoqueMinimo = BigDecimal.ZERO;
        }
    }
}
