package br.com.artecriativa.api.common;

/**
 * Lançada quando um recurso buscado por id (produto, matéria-prima, etc.) não existe.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
