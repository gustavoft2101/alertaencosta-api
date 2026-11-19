package br.com.fiap.alertaencosta.web.dto;

import br.com.fiap.alertaencosta.domain.model.NivelRisco;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Previsao publicada pelo modelo de Machine Learning (Python) para um setor.")
public record PrevisaoRequest(
        @Schema(example = "ALTO") @NotNull NivelRisco nivel,
        @Schema(example = "0.88", description = "Probabilidade da classe prevista (0 a 1).")
        @NotNull @DecimalMin("0") @DecimalMax("1") BigDecimal probabilidade,
        @Schema(example = "rf-v1.0") @Size(max = 30) String versaoModelo
) {
}
