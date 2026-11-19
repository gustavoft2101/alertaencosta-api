package br.com.fiap.alertaencosta.web.handler;

import br.com.fiap.alertaencosta.domain.exception.ConflitoException;
import br.com.fiap.alertaencosta.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.alertaencosta.domain.exception.RegraNegocioException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Formato unico de erro para toda a API, seguindo a RFC 7807 (Problem Details for HTTP APIs):
 * type, title, status, detail, instance + extensoes timestamp e (quando houver) erros por campo.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String TYPE_BASE = "https://alertaencosta.fiap.com.br/erros/";

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ProblemDetail naoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest req) {
        return problema(HttpStatus.NOT_FOUND, "recurso-nao-encontrado", "Recurso nao encontrado", ex.getMessage(), req);
    }

    @ExceptionHandler(ConflitoException.class)
    public ProblemDetail conflito(ConflitoException ex, HttpServletRequest req) {
        return problema(HttpStatus.CONFLICT, "conflito", "Conflito com o estado atual do recurso", ex.getMessage(), req);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ProblemDetail regraNegocio(RegraNegocioException ex, HttpServletRequest req) {
        return problema(HttpStatus.UNPROCESSABLE_ENTITY, "regra-de-negocio", "Regra de negocio violada", ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validacao(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }
        ProblemDetail pd = problema(HttpStatus.BAD_REQUEST, "dados-invalidos", "Dados de entrada invalidos",
                "Um ou mais campos estao invalidos. Veja 'campos' para detalhes.", req);
        pd.setProperty("campos", campos);
        return pd;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail corpoInvalido(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return problema(HttpStatus.BAD_REQUEST, "json-invalido", "Corpo da requisicao invalido",
                "JSON malformado ou com valor incompativel (verifique enums e tipos numericos).", req);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, PropertyReferenceException.class,
            InvalidDataAccessApiUsageException.class, MissingServletRequestParameterException.class,
            ConstraintViolationException.class, HandlerMethodValidationException.class})
    public ProblemDetail parametroInvalido(Exception ex, HttpServletRequest req) {
        return problema(HttpStatus.BAD_REQUEST, "parametro-invalido", "Parametro invalido", ex.getMessage(), req);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail integridade(DataIntegrityViolationException ex, HttpServletRequest req) {
        log.warn("Violacao de integridade: {}", ex.getMostSpecificCause().getMessage());
        return problema(HttpStatus.CONFLICT, "integridade-dados", "Violacao de integridade",
                "A operacao viola uma restricao do banco de dados (registro duplicado ou em uso).", req);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail metodoNaoSuportado(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return problema(HttpStatus.METHOD_NOT_ALLOWED, "metodo-nao-suportado", "Metodo HTTP nao suportado", ex.getMessage(), req);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ProblemDetail midiaNaoSuportada(HttpMediaTypeNotSupportedException ex, HttpServletRequest req) {
        return problema(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "midia-nao-suportada", "Content-Type nao suportado",
                "Envie o corpo como application/json.", req);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail rotaInexistente(NoResourceFoundException ex, HttpServletRequest req) {
        return problema(HttpStatus.NOT_FOUND, "rota-inexistente", "Endpoint inexistente",
                "Nao existe endpoint para " + req.getMethod() + " " + req.getRequestURI(), req);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail inesperado(Exception ex, HttpServletRequest req) {
        log.error("Erro inesperado em {} {}", req.getMethod(), req.getRequestURI(), ex);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "erro-interno", "Erro interno",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.", req);
    }

    private ProblemDetail problema(HttpStatus status, String tipo, String titulo, String detalhe, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detalhe);
        pd.setType(URI.create(TYPE_BASE + tipo));
        pd.setTitle(titulo);
        pd.setInstance(URI.create(req.getRequestURI()));
        pd.setProperty("timestamp", OffsetDateTime.now());
        return pd;
    }
}
