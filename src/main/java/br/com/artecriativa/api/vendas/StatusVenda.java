package br.com.artecriativa.api.vendas;

/**
 * Estágio de uma encomenda (venda com {@code dataEntregaPrevista}). Ordem importa:
 * {@link VendaService#avancarStatus} avança sequencialmente via {@code values()[ordinal()+1]}.
 * Venda de balcão (sem data de entrega) nasce direto em {@link #ENTREGUE}.
 */
public enum StatusVenda {
    PENDENTE,
    EM_PRODUCAO,
    PRONTO,
    ENTREGUE
}
