package br.com.fiap.alertaencosta.domain.model;

/**
 * Necessidade declarada no cadastro ("preciso de ajuda para sair").
 * Qualquer valor diferente de NENHUMA torna o morador prioritario na busca ativa.
 */
public enum NecessidadeEspecial {
    NENHUMA,
    IDOSO,
    MOBILIDADE_REDUZIDA,
    DEFICIENCIA_VISUAL,
    DEFICIENCIA_AUDITIVA,
    OUTRA;

    public boolean isPrioritario() {
        return this != NENHUMA;
    }
}
