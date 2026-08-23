package br.com.artecriativa.api.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Traduz exceções da aplicação em respostas HTTP padronizadas, em português.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> tratarNaoEncontrado(RecursoNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErroResposta(Instant.now(), HttpStatus.NOT_FOUND.value(), ex.getMessage(), null));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErroResposta> tratarEstadoInvalido(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErroResposta(Instant.now(), HttpStatus.UNPROCESSABLE_ENTITY.value(), ex.getMessage(), null));
    }

    /**
     * Violação de constraint do banco. O caso mais comum é uma FK ao tentar excluir um
     * produto/matéria-prima que já tem movimentações, vendas, receita ou tutorial
     * vinculados — o service não valida isso antecipadamente (não vale a pena checar
     * cada relação uma a uma), deixamos o banco recusar e traduzimos aqui.
     *
     * Mas essa mesma exceção também pode vir de um POST/PUT (ex: violação de UNIQUE ou
     * NOT NULL) — nesses casos a mensagem de "não é possível excluir" é enganosa, então
     * só a usamos quando a requisição era de fato um DELETE.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResposta> tratarViolacaoIntegridade(DataIntegrityViolationException ex,
                                                                    HttpServletRequest requisicao) {
        String mensagem = "DELETE".equalsIgnoreCase(requisicao.getMethod())
                ? "Não é possível excluir: existem outros registros vinculados a este item "
                        + "(ex: movimentações, vendas, receita ou tutorial)."
                : "Não foi possível salvar: os dados informados violam uma restrição do banco "
                        + "(ex: valor duplicado ou combinação já existente). Confira os campos e tente novamente.";
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErroResposta(Instant.now(), HttpStatus.CONFLICT.value(), mensagem, null));
    }

    /**
     * Lock otimista (`@Version`): alguém já salvou esse mesmo registro entre a leitura
     * e o save desta requisição (ex: duas movimentações de estoque quase simultâneas
     * no mesmo produto/matéria-prima). Antes disso existir, essa corrida sobrescrevia
     * em silêncio; agora responde 409 pra quem chamou tentar de novo.
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErroResposta> tratarConflitoVersao(ObjectOptimisticLockingFailureException ex) {
        String mensagem = "Este registro foi alterado por outra operação enquanto você estava "
                + "editando. Recarregue e tente novamente.";
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErroResposta(Instant.now(), HttpStatus.CONFLICT.value(), mensagem, null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> tratarValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() == null ? "inválido" : fe.getDefaultMessage(),
                        (a, b) -> a));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErroResposta(Instant.now(), HttpStatus.BAD_REQUEST.value(), "Dados inválidos", campos));
    }

    public record ErroResposta(Instant momento, int status, String mensagem, Map<String, String> campos) {
    }
}
