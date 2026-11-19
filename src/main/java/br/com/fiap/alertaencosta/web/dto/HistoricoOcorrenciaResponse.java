package br.com.fiap.alertaencosta.web.dto;

import br.com.fiap.alertaencosta.domain.model.HistoricoOcorrencia;
import br.com.fiap.alertaencosta.domain.model.StatusOcorrencia;

import java.time.LocalDateTime;

public record HistoricoOcorrenciaResponse(StatusOcorrencia statusAnterior, StatusOcorrencia statusNovo,
                                          String observacao, LocalDateTime alteradoEm) {

    public static HistoricoOcorrenciaResponse de(HistoricoOcorrencia h) {
        return new HistoricoOcorrenciaResponse(h.getStatusAnterior(), h.getStatusNovo(), h.getObservacao(), h.getAlteradoEm());
    }
}
