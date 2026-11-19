package br.com.fiap.alertaencosta.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Map;

@Schema(description = "Indicadores de impacto do piloto (secao 2 da proposta AlertaEncosta).")
public record IndicadoresResponse(
        @Schema(description = "Quantidade de setores em cada nivel de risco.") Map<String, Long> setoresPorNivel,
        @Schema(example = "1") long alertasAtivos,
        @Schema(example = "6") long moradoresCadastrados,
        @Schema(example = "3") long moradoresPrioritarios,
        @Schema(example = "3", description = "Moradores dos setores com alerta ativo.") long moradoresEmSetoresComAlerta,
        @Schema(example = "1") long confirmaramSeguros,
        @Schema(example = "1") long pedidosDeAjudaEmAberto,
        @Schema(example = "33.33", description = "% dos moradores alertados que confirmaram estar em local seguro.")
        BigDecimal percentualSeguros,
        @Schema(description = "Reportes por status.") Map<String, Long> ocorrenciasPorStatus,
        @Schema(example = "12") long leiturasUltimas24h
) {
}
