package br.com.fiap.alertaencosta.domain.exception;

/** Violacao de regra de negocio do dominio (HTTP 422). */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
