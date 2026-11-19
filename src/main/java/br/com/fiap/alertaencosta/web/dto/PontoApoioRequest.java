package br.com.fiap.alertaencosta.web.dto;

import br.com.fiap.alertaencosta.domain.model.SituacaoPontoApoio;
import br.com.fiap.alertaencosta.domain.model.TipoPontoApoio;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Local que recebe moradores evacuados.")
public record PontoApoioRequest(
        @Schema(example = "EE Viela da Paz (simulado)") @NotBlank @Size(max = 120) String nome,
        @Schema(example = "ESCOLA") @NotNull TipoPontoApoio tipo,
        @Schema(example = "Rua da Paz, 100 - Vila Brasilandia") @NotBlank @Size(max = 200) String endereco,
        @Schema(example = "-23.447000") @NotNull @DecimalMin("-90") @DecimalMax("90") BigDecimal latitude,
        @Schema(example = "-46.697500") @NotNull @DecimalMin("-180") @DecimalMax("180") BigDecimal longitude,
        @Schema(example = "200", description = "Pessoas que o local comporta.") @NotNull @Min(1) @Max(10000) Integer capacidade,
        @Schema(example = "ATIVO", description = "Opcional, padrao ATIVO. INATIVO nunca entra na rota segura.") SituacaoPontoApoio situacao
) {
}
