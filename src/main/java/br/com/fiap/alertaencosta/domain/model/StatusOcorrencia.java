package br.com.fiap.alertaencosta.domain.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Fluxo de atendimento de um reporte pela Defesa Civil.
 * <pre>
 * ABERTA -> EM_VISTORIA -> RESOLVIDA
 *    \_________\_________-> DESCARTADA
 * </pre>
 */
public enum StatusOcorrencia {
    ABERTA,
    EM_VISTORIA,
    RESOLVIDA,
    DESCARTADA;

    public Set<StatusOcorrencia> proximosPermitidos() {
        return switch (this) {
            case ABERTA -> EnumSet.of(EM_VISTORIA, DESCARTADA);
            case EM_VISTORIA -> EnumSet.of(RESOLVIDA, DESCARTADA);
            case RESOLVIDA, DESCARTADA -> EnumSet.noneOf(StatusOcorrencia.class);
        };
    }

    public boolean podeTransicionarPara(StatusOcorrencia novo) {
        return proximosPermitidos().contains(novo);
    }

    public boolean isTerminal() {
        return this == RESOLVIDA || this == DESCARTADA;
    }
}
