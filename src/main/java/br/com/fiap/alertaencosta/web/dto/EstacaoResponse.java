package br.com.fiap.alertaencosta.web.dto;

import br.com.fiap.alertaencosta.domain.model.EstacaoMonitoramento;
import br.com.fiap.alertaencosta.domain.model.SituacaoEstacao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EstacaoResponse(
        @Schema(example = "2") Long id,
        @Schema(example = "ESP32-BRA-02") String codigo,
        Referencia setor,
        @Schema(example = "ATIVA") SituacaoEstacao situacao,
        BigDecimal latitude,
        BigDecimal longitude,
        LocalDateTime instaladaEm,
        LocalDateTime ultimaLeituraEm
) {
    public static EstacaoResponse de(EstacaoMonitoramento e) {
        return new EstacaoResponse(e.getId(), e.getCodigo(), SetorResponse.ref(e.getSetor()), e.getSituacao(),
                e.getLatitude(), e.getLongitude(), e.getInstaladaEm(), e.getUltimaLeituraEm());
    }
}
