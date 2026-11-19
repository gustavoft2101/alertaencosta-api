package br.com.fiap.alertaencosta.web.dto;

import br.com.fiap.alertaencosta.domain.model.NivelRisco;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resultado do processamento de uma leitura ou previsao: novo nivel do setor e alerta gerado (se houver).")
public record RiscoProcessadoResponse(
        @Schema(description = "Leitura gravada (nula quando a entrada foi uma previsao do modelo).") LeituraResponse leitura,
        PrevisaoResponse previsao,
        @Schema(example = "MEDIO") NivelRisco nivelAnterior,
        @Schema(example = "ALTO") NivelRisco nivelAtual,
        @Schema(description = "Alerta emitido ou escalado por esta entrada (nulo se nenhum).") AlertaResumo alerta,
        @Schema(example = "ALERTA_EMITIDO", description = "NENHUMA, ALERTA_EMITIDO, ALERTA_ESCALADO ou ALERTA_JA_ATIVO.")
        String acao
) {
}
