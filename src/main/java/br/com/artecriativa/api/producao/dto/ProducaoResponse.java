package br.com.artecriativa.api.producao.dto;

import br.com.artecriativa.api.producao.Producao;

import java.math.BigDecimal;
import java.time.Instant;

public record ProducaoResponse(
        Long id,
        Long produtoId,
        String produtoNome,
        BigDecimal quantidadeProduzida,
        BigDecimal custoTotal,
        String observacao,
        Instant dataProducao
) {
    public static ProducaoResponse de(Producao producao) {
        return new ProducaoResponse(
                producao.getId(),
                producao.getProduto().getId(),
                producao.getProduto().getNome(),
                producao.getQuantidadeProduzida(),
                producao.getCustoTotal(),
                producao.getObservacao(),
                producao.getDataProducao()
        );
    }
}
