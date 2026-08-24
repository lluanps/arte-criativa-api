package br.com.artecriativa.api.producao.dto;

import br.com.artecriativa.api.estoque.UnidadeMedida;
import br.com.artecriativa.api.producao.Receita;
import br.com.artecriativa.api.producao.ReceitaItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

public record ReceitaResponse(
        Long id,
        Long produtoId,
        String produtoNome,
        String nome,
        BigDecimal rendimento,
        List<ReceitaItemResponse> itens,
        /** Custo só de matéria-prima (insumo), por unidade — não inclui mão de obra nem
         * embalagem/outros. Ver {@code custoTotal} pro custo real. */
        BigDecimal custoProducao,
        BigDecimal custoMaoDeObra,
        BigDecimal custoEmbalagemOutros,
        /** custoProducao + custoMaoDeObra + custoEmbalagemOutros — é este valor (não
         * custoProducao) que embasa margemLucro/margemPercentual/precoSugerido abaixo. */
        BigDecimal custoTotal,
        BigDecimal margemLucro,
        BigDecimal margemPercentual,
        BigDecimal margemDesejadaPercentual,
        BigDecimal precoSugerido,
        BigDecimal precoMercadoMin,
        BigDecimal precoMercadoMax,
        Instant precoMercadoAtualizadoEm,
        Instant criadoEm,
        /** Quantas unidades do produto dá pra produzir agora, considerando o estoque
         * atual de TODAS as matérias-primas da receita — o mínimo entre
         * {@code itens[].unidadesProduziveisComEsteItem} (a matéria-prima mais escassa
         * é quem manda). {@code null} só se não der pra calcular pra nenhum item
         * (receita sem itens válidos). */
        Long quantidadeProduzivelComEstoqueAtual,
        /** Nome da matéria-prima que é o gargalo (a que gerou o mínimo acima) — ajuda a
         * UI mostrar "falta X" em vez de só o número. {@code null} junto com o campo
         * acima. */
        String materiaPrimaLimitanteNome
) {
    /** Margem alvo usada quando o produto não tem uma própria configurada — 200% (preço
     * = 3x o custo de matéria-prima) é a referência comum pra artesanato, já que esse
     * custo não inclui mão de obra nem outros custos indiretos. */
    private static final BigDecimal MARGEM_DESEJADA_PADRAO = BigDecimal.valueOf(200);

    public static ReceitaResponse de(Receita receita) {
        BigDecimal custoProducao = calcularCustoProducao(receita);
        BigDecimal custoMaoDeObra = receita.getCustoMaoDeObra() != null ? receita.getCustoMaoDeObra() : BigDecimal.ZERO;
        BigDecimal custoEmbalagemOutros = receita.getCustoEmbalagemOutros() != null ? receita.getCustoEmbalagemOutros() : BigDecimal.ZERO;
        BigDecimal custoTotal = custoProducao.add(custoMaoDeObra).add(custoEmbalagemOutros);

        BigDecimal precoVenda = receita.getProduto().getPrecoVenda();
        BigDecimal margemLucro = precoVenda != null ? precoVenda.subtract(custoTotal) : null;
        BigDecimal margemPercentual = (margemLucro != null && precoVenda != null && precoVenda.compareTo(BigDecimal.ZERO) > 0)
                ? margemLucro.divide(precoVenda, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP)
                : null;

        BigDecimal margemDesejada = receita.getProduto().getMargemDesejadaPercentual() != null
                ? receita.getProduto().getMargemDesejadaPercentual()
                : MARGEM_DESEJADA_PADRAO;
        BigDecimal precoSugerido = custoTotal
                .multiply(BigDecimal.ONE.add(margemDesejada.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)))
                .setScale(2, RoundingMode.HALF_UP);

        var categoria = receita.getProduto().getCategoria();
        List<ReceitaItemResponse> itens = receita.getItens().stream().map(ReceitaItemResponse::de).toList();

        ReceitaItemResponse itemLimitante = itens.stream()
                .filter(item -> item.unidadesProduziveisComEsteItem() != null)
                .min(Comparator.comparing(ReceitaItemResponse::unidadesProduziveisComEsteItem))
                .orElse(null);

        return new ReceitaResponse(
                receita.getId(),
                receita.getProduto().getId(),
                receita.getProduto().getNome(),
                receita.getNome(),
                receita.getRendimento(),
                itens,
                custoProducao,
                custoMaoDeObra,
                custoEmbalagemOutros,
                custoTotal,
                margemLucro,
                margemPercentual,
                margemDesejada,
                precoSugerido,
                categoria != null ? categoria.getPrecoMercadoMin() : null,
                categoria != null ? categoria.getPrecoMercadoMax() : null,
                categoria != null ? categoria.getPrecoMercadoAtualizadoEm() : null,
                receita.getCriadoEm(),
                itemLimitante != null ? itemLimitante.unidadesProduziveisComEsteItem() : null,
                itemLimitante != null ? itemLimitante.materiaPrimaNome() : null
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
            BigDecimal quantidadeNaUnidadeDaMateriaPrima = UnidadeMedida.converter(
                    item.getQuantidade(), item.getUnidadeMedida(), item.getMateriaPrima().getUnidadeMedida());
            custoTotal = custoTotal.add(quantidadeNaUnidadeDaMateriaPrima.multiply(item.getMateriaPrima().getCustoUnitario()));
        }
        BigDecimal rendimento = receita.getRendimento();
        if (rendimento == null || rendimento.compareTo(BigDecimal.ZERO) <= 0) {
            return custoTotal.setScale(2, RoundingMode.HALF_UP);
        }
        return custoTotal.divide(rendimento, 2, RoundingMode.HALF_UP);
    }
}
