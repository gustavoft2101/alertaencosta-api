package br.com.fiap.alertaencosta.web.dto;

import br.com.fiap.alertaencosta.domain.model.TipoRisco;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Cadastro de setor de risco. Os limites definem quando o setor entra em risco ALTO.")
public record SetorRequest(
        @Schema(example = "BR-05", description = "Codigo unico e imutavel: 2 letras, hifen, 2 digitos.")
        @NotBlank @Pattern(regexp = "^[A-Za-z]{2}-\\d{2}$", message = "use o formato AA-00 (ex.: BR-05)") String codigo,
        @Schema(example = "Viela da Paz") @NotBlank @Size(max = 100) String nome,
        @Schema(example = "Vila Brasilandia") @NotBlank @Size(max = 80) String bairro,
        @Schema(example = "DESLIZAMENTO") @NotNull TipoRisco tipoRisco,
        @Schema(example = "-23.448000") @NotNull @DecimalMin("-90") @DecimalMax("90") BigDecimal latitude,
        @Schema(example = "-46.699000") @NotNull @DecimalMin("-180") @DecimalMax("180") BigDecimal longitude,
        @Schema(example = "70.0", description = "Chuva acumulada em 24 h (mm) que coloca o setor em risco ALTO.")
        @NotNull @DecimalMin(value = "0", inclusive = false) @DecimalMax("500") BigDecimal limiteChuvaMm,
        @Schema(example = "85.0", description = "Umidade do solo (%) que coloca o setor em risco ALTO.")
        @NotNull @DecimalMin(value = "0", inclusive = false) @DecimalMax("100") BigDecimal limiteUmidadeSoloPct,
        @Schema(example = "120.0", description = "Opcional. Nivel do corrego (cm), para setores alagaveis.")
        @DecimalMin(value = "0", inclusive = false) @DecimalMax("1000") BigDecimal limiteNivelAguaCm
) {
}
