package br.com.fiap.alertaencosta.web.dto;

import br.com.fiap.alertaencosta.domain.model.NecessidadeEspecial;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Morador do setor que ainda nao esta seguro: pediu ajuda ou nao respondeu.")
public record PendenciaResponse(
        Long moradorId,
        String nome,
        String telefone,
        String endereco,
        NecessidadeEspecial necessidadeEspecial,
        boolean prioritario,
        @Schema(example = "PRECISA_AJUDA", description = "PRECISA_AJUDA ou SEM_RESPOSTA") String situacao,
        String observacao
) {
}
