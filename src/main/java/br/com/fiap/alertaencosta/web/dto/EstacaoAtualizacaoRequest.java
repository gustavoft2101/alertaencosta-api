package br.com.fiap.alertaencosta.web.dto;

import br.com.fiap.alertaencosta.domain.model.SituacaoEstacao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Substituicao completa (PUT) da estacao. O codigo nao muda.")
public record EstacaoAtualizacaoRequest(
        @Schema(example = "5") @NotNull Long setorId,
        @NotNull @DecimalMin("-90") @DecimalMax("90") BigDecimal latitude,
        @NotNull @DecimalMin("-180") @DecimalMax("180") BigDecimal longitude,
        @Schema(example = "MANUTENCAO") @NotNull SituacaoEstacao situacao
) {
}
