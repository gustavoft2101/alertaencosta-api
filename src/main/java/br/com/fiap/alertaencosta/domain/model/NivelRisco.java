package br.com.fiap.alertaencosta.domain.model;

/** Nivel de risco de um setor, como no modelo de previsao (baixo, medio, alto). */
public enum NivelRisco {
    BAIXO,
    MEDIO,
    ALTO;

    public boolean isMaiorQue(NivelRisco outro) {
        return this.ordinal() > outro.ordinal();
    }

    public NivelRisco maior(NivelRisco outro) {
        return isMaiorQue(outro) ? this : outro;
    }
}
