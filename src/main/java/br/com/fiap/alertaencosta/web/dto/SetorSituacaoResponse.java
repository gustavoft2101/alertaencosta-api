package br.com.fiap.alertaencosta.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Retrato do setor para o painel da Defesa Civil.")
public record SetorSituacaoResponse(
        SetorResponse setor,
        @Schema(description = "Leitura mais recente entre as estacoes do setor (nula se nunca houve leitura).")
        LeituraResponse ultimaLeitura,
        @Schema(description = "Alerta ativo do setor (nulo se nao houver).") AlertaResumo alertaAtivo,
        @Schema(example = "3") long totalMoradores,
        @Schema(example = "2", description = "Idosos e pessoas com deficiencia/mobilidade reduzida.") long moradoresPrioritarios,
        @Schema(example = "1") long estacoesAtivas
) {
}
