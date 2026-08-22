package br.com.artecriativa.api.estoque.dto;

import br.com.artecriativa.api.estoque.MotivoMovimentacaoMateriaPrima;
import br.com.artecriativa.api.estoque.TipoMovimentacao;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MovimentacaoMateriaPrimaRequest(
        @NotNull(message = "tipo é obrigatório") TipoMovimentacao tipo,
        @NotNull(message = "motivo é obrigatório") MotivoMovimentacaoMateriaPrima motivo,
        @NotNull(message = "quantidade é obrigatória")
        @DecimalMin(value = "0.0", inclusive = false, message = "quantidade deve ser maior que zero") BigDecimal quantidade,
        /** Opcional — só numa ENTRADA. Quanto foi pago no TOTAL desta compra (ex: 3kg de
         * cera por R$ 100 → valorPago = 100). O sistema calcula o custo unitário sozinho
         * (valorPago ÷ quantidade) e atualiza o custo médio da matéria-prima. Deixe em
         * branco pra manter o custo unitário atual (ex: entrada de ajuste, sem compra). */
        @DecimalMin(value = "0.0", message = "valor pago não pode ser negativo") BigDecimal valorPago,
        String observacao
) {
}
