package br.com.fiap.alertaencosta.web.dto;

import br.com.fiap.alertaencosta.domain.model.PontoApoio;
import br.com.fiap.alertaencosta.domain.model.SituacaoPontoApoio;
import br.com.fiap.alertaencosta.domain.model.TipoPontoApoio;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record PontoApoioResponse(
        @Schema(example = "2") Long id,
        @Schema(example = "Paroquia Nossa Senhora (simulado)") String nome,
        @Schema(example = "IGREJA") TipoPontoApoio tipo,
        String endereco,
        BigDecimal latitude,
        BigDecimal longitude,
        @Schema(example = "150") Integer capacidade,
        @Schema(example = "ATIVO") SituacaoPontoApoio situacao
) {
    public static PontoApoioResponse de(PontoApoio p) {
        return p == null ? null : new PontoApoioResponse(p.getId(), p.getNome(), p.getTipo(), p.getEndereco(),
                p.getLatitude(), p.getLongitude(), p.getCapacidade(), p.getSituacao());
    }
}
