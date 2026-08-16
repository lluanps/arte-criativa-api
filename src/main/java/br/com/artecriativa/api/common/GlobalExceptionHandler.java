package br.com.artecriativa.api.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
