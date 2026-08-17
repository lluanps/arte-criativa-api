package br.com.artecriativa.api.producao.dto;

import br.com.artecriativa.api.producao.Receita;
import br.com.artecriativa.api.producao.ReceitaItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

public record ReceitaResponse(
        Long id,
        Long produtoId,
        String produtoNome,
        String nome,
        BigDecimal rendimento,
        List<ReceitaItemResponse> itens,
        BigDecimal custoProducao,
        BigDecimal margemLucro,
        BigDecimal margemPercentual,
        Instant criadoEm
) {
    public static ReceitaResponse de(Receita receita) {
        BigDecimal custoProducao = calcularCustoProducao(receita);
        BigDecimal precoVenda = receita.getProduto().getPrecoVenda();
        BigDecimal margemLucro = precoVenda != null ? precoVenda.subtract(custoProducao) : null;
        BigDecimal margemPercentual = (margemLucro != null && precoVenda != null && precoVenda.compareTo(BigDecimal.ZERO) > 0)
                ? margemLucro.divide(precoVenda, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP)
                : null;

        return new ReceitaResponse(
                receita.getId(),
                receita.getProduto().getId(),
                receita.getProduto().getNome(),
                receita.getNome(),
                receita.getRendimento(),
                receita.getItens().stream().map(ReceitaItemResponse::de).toList(),
                custoProducao,
                margemLucro,
                margemPercentual,
                receita.getCriadoEm()
        );
    }

    /**
     * Custo de matéria-prima pra produzir UMA unidade do produto: soma do custo de cada
     * item da receita (quantidade × custo unitário da matéria-prima) dividido pelo
     * rendimento da receita. Não inclui mão de obra nem outros custos indiretos — é só
     * o custo de insumo, uma estimativa de piso pra margem.
     */
    private static BigDecimal calcularCustoProducao(Receita receita) {
        BigDecimal custoTotal = BigDecimal.ZERO;
        for (ReceitaItem item : receita.getItens()) {
            custoTotal = custoTotal.add(item.getQuantidade().multiply(item.getMateriaPrima().getCustoUnitario()));
        }
        BigDecimal rendimento = receita.getRendimento();
        if (rendimento == null || rendimento.compareTo(BigDecimal.ZERO) <= 0) {
            return custoTotal.setScale(2, RoundingMode.HALF_UP);
        }
        return custoTotal.divide(rendimento, 2, RoundingMode.HALF_UP);
    }
}
