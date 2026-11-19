package br.com.fiap.alertaencosta.web.dto;

/** Referencia resumida a outro recurso, para nao expor o objeto inteiro. */
public record Referencia(Long id, String codigo, String nome) {
}
