package br.com.fiap.alertaencosta.web.dto;

import br.com.fiap.alertaencosta.domain.model.Alerta;
import br.com.fiap.alertaencosta.domain.model.Geo;
import br.com.fiap.alertaencosta.domain.model.NivelRisco;
import br.com.fiap.alertaencosta.domain.model.OrigemAlerta;
import br.com.fiap.alertaencosta.domain.model.SituacaoMorador;
import br.com.fiap.alertaencosta.domain.model.StatusAlerta;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Alerta com mensagem, rota e acompanhamento das confirmacoes.")
public record AlertaResponse(
        @Schema(example = "1") Long id,
        Referencia setor,
        @Schema(example = "ALTO") NivelRisco nivel,
        @Schema(example = "ATIVO") StatusAlerta status,
        @Schema(example = "AUTOMATICO") OrigemAlerta origem,
        String mensagem,
        PontoApoioResponse pontoApoio,
        @Schema(example = "438") Integer distanciaMetros,
        @Schema(example = "7") Integer tempoEstimadoMinutos,
        String linkMapa,
        @Schema(example = "3") Integer totalNotificados,
        @Schema(example = "1") long confirmadosSeguros,
        @Schema(example = "1") long pedidosDeAjuda,
        LocalDateTime emitidoEm,
        LocalDateTime encerradoEm,
        String motivoEncerramento
) {
    public static AlertaResponse de(Alerta a) {
        var ponto = a.getPontoApoio();
        return new AlertaResponse(a.getId(), SetorResponse.ref(a.getSetor()), a.getNivel(), a.getStatus(), a.getOrigem(),
                a.getMensagem(), PontoApoioResponse.de(ponto), a.getDistanciaMetros(),
                a.getDistanciaMetros() == null ? null : Geo.minutosAPe(a.getDistanciaMetros()),
                ponto == null ? null : RotaSeguraResponse.linkMapa(a.getSetor().getLatitude(), a.getSetor().getLongitude(), ponto),
                a.getTotalNotificados(), a.contar(SituacaoMorador.SEGURO), a.contar(SituacaoMorador.PRECISA_AJUDA),
                a.getEmitidoEm(), a.getEncerradoEm(), a.getMotivoEncerramento());
    }
}
