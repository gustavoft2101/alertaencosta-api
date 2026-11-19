package br.com.fiap.alertaencosta.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Leitura enviada pelo firmware do ESP32.")
public record LeituraRequest(
        @Schema(example = "42.5", description = "Chuva acumulada nas ultimas 24 h (mm).")
        @NotNull @DecimalMin("0") @DecimalMax("500") BigDecimal chuvaMm,
        @Schema(example = "78.0", description = "Umidade do solo (%).")
        @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal umidadeSoloPct,
        @Schema(example = "35.0", description = "Nivel da agua no corrego (cm). Opcional: nem toda estacao tem o sensor ultrassonico.")
        @DecimalMin("0") @DecimalMax("1000") BigDecimal nivelAguaCm,
        @Schema(example = "2026-11-18T18:30:00", description = "Opcional; se ausente, usa o horario do servidor.")
        LocalDateTime medidaEm
) {
}
