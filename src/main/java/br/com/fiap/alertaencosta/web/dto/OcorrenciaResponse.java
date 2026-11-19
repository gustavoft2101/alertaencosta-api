package br.com.fiap.alertaencosta.web.dto;

import br.com.fiap.alertaencosta.domain.model.Ocorrencia;
import br.com.fiap.alertaencosta.domain.model.StatusOcorrencia;
import br.com.fiap.alertaencosta.domain.model.TipoOcorrencia;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

public record OcorrenciaResponse(
        @Schema(example = "4") Long id,
        @Schema(example = "AE-2026-K7P2QX") String protocolo,
        @Schema(example = "TRINCA_SOLO") TipoOcorrencia tipo,
        String descricao,
        String fotoUrl,
        BigDecimal latitude,
        BigDecimal longitude,
        @Schema(example = "ABERTA") StatusOcorrencia status,
        Set<StatusOcorrencia> proximosStatusPermitidos,
        Referencia morador,
        Referencia setor,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm,
        LocalDateTime resolvidoEm
) {
    public static OcorrenciaResponse de(Ocorrencia o) {
        return new OcorrenciaResponse(o.getId(), o.getProtocolo(), o.getTipo(), o.getDescricao(), o.getFotoUrl(),
                o.getLatitude(), o.getLongitude(), o.getStatus(), o.getStatus().proximosPermitidos(),
                new Referencia(o.getMorador().getId(), null, o.getMorador().getNome()),
                SetorResponse.ref(o.getSetor()), o.getCriadoEm(), o.getAtualizadoEm(), o.getResolvidoEm());
    }
}
