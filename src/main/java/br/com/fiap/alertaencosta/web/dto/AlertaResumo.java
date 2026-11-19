package br.com.fiap.alertaencosta.web.dto;

import br.com.fiap.alertaencosta.domain.model.Alerta;
import br.com.fiap.alertaencosta.domain.model.NivelRisco;
import br.com.fiap.alertaencosta.domain.model.StatusAlerta;

import java.time.LocalDateTime;

public record AlertaResumo(Long id, NivelRisco nivel, StatusAlerta status, Integer totalNotificados, LocalDateTime emitidoEm) {

    public static AlertaResumo de(Alerta a) {
        return a == null ? null : new AlertaResumo(a.getId(), a.getNivel(), a.getStatus(), a.getTotalNotificados(), a.getEmitidoEm());
    }
}
