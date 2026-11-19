package br.com.fiap.alertaencosta.domain.model;

/** Estado operacional da estacao ESP32. So estacoes ATIVA podem enviar leituras. */
public enum SituacaoEstacao {
    ATIVA,
    MANUTENCAO,
    INATIVA
}
