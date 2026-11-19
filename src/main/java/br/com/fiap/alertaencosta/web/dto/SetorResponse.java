package br.com.fiap.alertaencosta.web.dto;

import br.com.fiap.alertaencosta.domain.model.NivelRisco;
import br.com.fiap.alertaencosta.domain.model.Setor;
import br.com.fiap.alertaencosta.domain.model.TipoRisco;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SetorResponse(
        @Schema(example = "2") Long id,
        @Schema(example = "BR-02") String codigo,
        @Schema(example = "Margem do Corrego Bananal") String nome,
        @Schema(example = "Vila Brasilandia") String bairro,
        @Schema(example = "ALAGAMENTO") TipoRisco tipoRisco,
        @Schema(example = "ALTO") NivelRisco nivelRiscoAtual,
        LocalDateTime riscoAtualizadoEm,
        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal limiteChuvaMm,
        BigDecimal limiteUmidadeSoloPct,
        BigDecimal limiteNivelAguaCm
) {
    public static SetorResponse de(Setor s) {
        return new SetorResponse(s.getId(), s.getCodigo(), s.getNome(), s.getBairro(), s.getTipoRisco(),
                s.getNivelRiscoAtual(), s.getRiscoAtualizadoEm(), s.getLatitude(), s.getLongitude(),
                s.getLimiteChuvaMm(), s.getLimiteUmidadeSoloPct(), s.getLimiteNivelAguaCm());
    }

    public static Referencia ref(Setor s) {
        return new Referencia(s.getId(), s.getCodigo(), s.getNome());
    }
}
