package br.com.fiap.alertaencosta.web.dto;

import br.com.fiap.alertaencosta.domain.model.NivelRisco;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Emissao manual de alerta pela Defesa Civil.")
public record AlertaRequest(
        @Schema(example = "1") @NotNull Long setorId,
        @Schema(example = "MEDIO", description = "MEDIO ou ALTO.") @NotNull NivelRisco nivel,
        @Schema(description = "Opcional. Se ausente, a API monta a mensagem com a rota.") @Size(max = 500) String mensagem,
        @Schema(description = "Opcional. Se ausente, usa o ponto de apoio ativo mais proximo do setor.") Long pontoApoioId
) {
}
