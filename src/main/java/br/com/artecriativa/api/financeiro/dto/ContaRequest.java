package br.com.artecriativa.api.financeiro.dto;

import br.com.artecriativa.api.financeiro.TipoConta;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ContaRequest(
        @NotNull(message = "tipo é obrigatório") TipoConta tipo,
        @NotBlank(message = "descrição é obrigatória") String descricao,
        @NotNull(message = "valor é obrigatório")
        @DecimalMin(value = "0.0", inclusive = false, message = "valor deve ser maior que zero") BigDecimal valor,
        @NotNull(message = "vencimento é obrigatório") LocalDate vencimento,
        /** Opcional — quando essa conta é a compra de matéria-prima em si (ex: nota
         * fiscal do fornecedor lançada como conta a pagar). Cada item vira uma entrada
         * de estoque vinculada a esta conta (sem lançar despesa duplicada — a despesa já
         * é a própria conta, quando paga). Só permitido em conta do tipo PAGAR, e
         * {@code soma(itens.valor) + custosExtras} tem que bater exato com {@link #valor}.
         * Nulo/vazio = conta normal, sem nenhuma mudança de comportamento. */
        @Valid List<ItemMateriaPrimaCompraRequest> itensMateriaPrima,
        /** Opcional (default 0) — frete/taxas/outros custos da mesma compra que não são
         * de nenhuma matéria-prima específica. Não gera movimentação de estoque nenhuma,
         * só entra na soma acima. Só faz sentido junto de {@link #itensMateriaPrima}. */
        @DecimalMin(value = "0.0", message = "custos extras não pode ser negativo") BigDecimal custosExtras
) {
}
