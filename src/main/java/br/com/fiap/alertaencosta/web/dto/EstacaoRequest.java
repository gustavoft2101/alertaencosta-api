package br.com.fiap.alertaencosta.web.dto;

import br.com.fiap.alertaencosta.domain.model.SituacaoEstacao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Cadastro de estacao ESP32.")
public record EstacaoRequest(
        @Schema(example = "ESP32-BRA-05", description = "Codigo unico gravado no firmware.") @NotBlank @Size(max = 30) String codigo,
        @Schema(example = "5") @NotNull Long setorId,
        @Schema(example = "-23.448100") @NotNull @DecimalMin("-90") @DecimalMax("90") BigDecimal latitude,
        @Schema(example = "-46.699100") @NotNull @DecimalMin("-180") @DecimalMax("180") BigDecimal longitude,
        @Schema(example = "ATIVA", description = "Opcional, padrao ATIVA.") SituacaoEstacao situacao
) {
}
