package br.com.artecriativa.api.producao.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record ReceitaRequest(
        @NotNull(message = "produto é obrigatório") Long produtoId,
        @NotBlank(message = "nome é obrigatório") String nome,
        @DecimalMin(value = "0.0", inclusive = false, message = "rendimento deve ser maior que zero") BigDecimal rendimento,
        @NotEmpty(message = "receita precisa de ao menos um item")
        List<@Valid ReceitaItemRequest> itens,
        /** Opcionais (default 0) — custo de mão de obra e de embalagem/outros custos
         * indiretos, por unidade produzida. Somados ao custo de insumo pra formar o
         * custo real da ficha técnica. */
        @DecimalMin(value = "0.0", message = "custo de mão de obra não pode ser negativo") BigDecimal custoMaoDeObra,
        @DecimalMin(value = "0.0", message = "custo de embalagem/outros não pode ser negativo") BigDecimal custoEmbalagemOutros
) {
    public ReceitaRequest {
        if (rendimento == null) {
            rendimento = BigDecimal.ONE;
        }
        if (custoMaoDeObra == null) {
            custoMaoDeObra = BigDecimal.ZERO;
        }
        if (custoEmbalagemOutros == null) {
            custoEmbalagemOutros = BigDecimal.ZERO;
        }
    }
}
