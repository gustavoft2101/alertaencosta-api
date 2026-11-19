package br.com.fiap.alertaencosta.web.dto;

import br.com.fiap.alertaencosta.domain.model.TipoOcorrencia;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Reporte do morador com foto e localizacao (GPS do celular).")
public record OcorrenciaRequest(
        @Schema(example = "TRINCA_SOLO") @NotNull TipoOcorrencia tipo,
        @Schema(example = "Rachadura nova no barranco atras da casa, com cerca de 2 metros.")
        @NotBlank @Size(min = 10, max = 1000) String descricao,
        @Schema(example = "https://storage.alertaencosta.app/fotos/simulada-001.jpg") @Size(max = 500) String fotoUrl,
        @Schema(example = "-23.448400") @NotNull @DecimalMin("-90") @DecimalMax("90") BigDecimal latitude,
        @Schema(example = "-46.699300") @NotNull @DecimalMin("-180") @DecimalMax("180") BigDecimal longitude,
        @Schema(example = "7") @NotNull Long moradorId
) {
}
