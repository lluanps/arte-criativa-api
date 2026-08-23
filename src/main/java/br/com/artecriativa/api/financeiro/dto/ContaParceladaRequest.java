package br.com.artecriativa.api.financeiro.dto;

import br.com.artecriativa.api.financeiro.TipoConta;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Registra uma conta parcelada de uma vez: gera {@code quantidadeParcelas} contas
 * independentes (ver {@code ContaService#criarParcelada}), uma por mês a partir de
 * {@code primeiroVencimento}, cada uma com {@code valorTotal ÷ quantidadeParcelas}
 * (resto de arredondamento fica na última parcela). {@code descricao} é o texto base —
 * cada parcela recebe " (parcela X/N)" no final automaticamente.
 * <p>
 * Pra uma conta à vista (1x), usar {@code ContaRequest} normal — esse endpoint exige
 * pelo menos 2 parcelas de propósito.
 */
public record ContaParceladaRequest(
        @NotNull(message = "tipo é obrigatório") TipoConta tipo,
        @NotBlank(message = "descrição é obrigatória") String descricao,
        @NotNull(message = "valor total é obrigatório")
        @DecimalMin(value = "0.0", inclusive = false, message = "valor total deve ser maior que zero") BigDecimal valorTotal,
        @NotNull(message = "quantidade de parcelas é obrigatória")
        @Min(value = 2, message = "quantidade de parcelas deve ser pelo menos 2 (pra 1x, use uma conta normal)") Integer quantidadeParcelas,
        @NotNull(message = "vencimento da primeira parcela é obrigatório") LocalDate primeiroVencimento
) {
}
