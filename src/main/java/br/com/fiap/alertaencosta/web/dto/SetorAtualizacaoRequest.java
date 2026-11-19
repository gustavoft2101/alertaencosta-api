package br.com.fiap.alertaencosta.web.dto;

import br.com.fiap.alertaencosta.domain.model.TipoRisco;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Substituicao completa (PUT) do setor. O codigo e o nivel de risco atual nao sao editaveis.")
public record SetorAtualizacaoRequest(
        @Schema(example = "Viela da Paz e Escadaria") @NotBlank @Size(max = 100) String nome,
        @Schema(example = "Vila Brasilandia") @NotBlank @Size(max = 80) String bairro,
        @Schema(example = "MISTO") @NotNull TipoRisco tipoRisco,
        @Schema(example = "-23.448000") @NotNull @DecimalMin("-90") @DecimalMax("90") BigDecimal latitude,
        @Schema(example = "-46.699000") @NotNull @DecimalMin("-180") @DecimalMax("180") BigDecimal longitude,
        @Schema(example = "65.0") @NotNull @DecimalMin(value = "0", inclusive = false) @DecimalMax("500") BigDecimal limiteChuvaMm,
        @Schema(example = "85.0") @NotNull @DecimalMin(value = "0", inclusive = false) @DecimalMax("100") BigDecimal limiteUmidadeSoloPct,
        @Schema(example = "120.0") @DecimalMin(value = "0", inclusive = false) @DecimalMax("1000") BigDecimal limiteNivelAguaCm
) {
}
