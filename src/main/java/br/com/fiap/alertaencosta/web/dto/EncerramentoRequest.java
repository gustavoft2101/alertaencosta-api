package br.com.fiap.alertaencosta.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EncerramentoRequest(
        @Schema(example = "Chuva cessou e vistoria nao encontrou novos sinais de movimentacao do solo.")
        @NotBlank @Size(max = 300) String motivo
) {
}
