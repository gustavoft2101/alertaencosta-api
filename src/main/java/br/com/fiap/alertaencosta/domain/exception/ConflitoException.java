package br.com.fiap.alertaencosta.domain.exception;

/** Conflito com o estado atual dos dados: duplicidade ou registro em uso (HTTP 409). */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
