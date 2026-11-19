package br.com.fiap.alertaencosta.domain.model;

/** Quem classificou o risco: a regra de limites da propria API ou o modelo de ML (Python). */
public enum OrigemPrevisao {
    REGRA_LIMITES,
    MODELO_ML
}
