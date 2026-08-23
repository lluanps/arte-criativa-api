package br.com.artecriativa.api.financeiro;

public enum OrigemLancamento {
    VENDA,
    COMPRA,
    /** Conta a pagar/receber marcada como paga (ver {@code ContaService}). */
    CONTA,
    MANUAL
}
