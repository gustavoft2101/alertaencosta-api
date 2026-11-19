package br.com.fiap.alertaencosta.web.dto;

import br.com.fiap.alertaencosta.domain.model.ConfirmacaoAlerta;
import br.com.fiap.alertaencosta.domain.model.SituacaoMorador;

import java.time.LocalDateTime;

public record ConfirmacaoResponse(
        Long alertaId,
        Long moradorId,
        String moradorNome,
        SituacaoMorador situacao,
        String observacao,
        LocalDateTime registradaEm
) {
    public static ConfirmacaoResponse de(ConfirmacaoAlerta c) {
        return new ConfirmacaoResponse(c.getAlerta().getId(), c.getMorador().getId(), c.getMorador().getNome(),
                c.getSituacao(), c.getObservacao(), c.getRegistradaEm());
    }
}
