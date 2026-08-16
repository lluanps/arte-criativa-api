package br.com.artecriativa.api.producao.dto;

import br.com.artecriativa.api.producao.Receita;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ReceitaResponse(
        Long id,
        Long produtoId,
        String produtoNome,
        String nome,
        BigDecimal rendimento,
        List<ReceitaItemResponse> itens,
        Instant criadoEm
) {
    public static ReceitaResponse de(Receita receita) {
        return new ReceitaResponse(
                receita.getId(),
                receita.getProduto().getId(),
                receita.getProduto().getNome(),
                receita.getNome(),
                receita.getRendimento(),
                receita.getItens().stream().map(ReceitaItemResponse::de).toList(),
                receita.getCriadoEm()
        );
    }
}
