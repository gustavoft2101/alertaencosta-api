package br.com.fiap.alertaencosta.domain.exception;

/** Lancada quando o recurso identificado pela URI nao existe (HTTP 404). */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String recurso, Object id) {
        super("%s com identificador '%s' nao encontrado(a).".formatted(recurso, id));
    }
}
