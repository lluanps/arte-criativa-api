package br.com.artecriativa.api.common;

/**
 * Lançada quando uma operação não pode prosseguir por um conflito de estado nos
 * dados — ex: excluir uma conta vinculada a compra de matéria-prima deixaria o
 * estoque negativo. Traduzida pra 409 em {@code GlobalExceptionHandler}.
 */
public class ConflitoOperacaoException extends RuntimeException {

    public ConflitoOperacaoException(String mensagem) {
        super(mensagem);
    }
}
