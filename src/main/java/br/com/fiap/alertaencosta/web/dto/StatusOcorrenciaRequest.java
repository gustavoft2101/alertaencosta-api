package br.com.fiap.alertaencosta.web.dto;

import br.com.fiap.alertaencosta.domain.model.StatusOcorrencia;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "ABERTA -> EM_VISTORIA -> RESOLVIDA; ABERTA/EM_VISTORIA -> DESCARTADA (exige observacao).")
public record StatusOcorrenciaRequest(
        @Schema(example = "EM_VISTORIA") @NotNull StatusOcorrencia status,
        @Schema(example = "Vistoria agendada para hoje as 16h.") @Size(max = 500) String observacao
) {
}
