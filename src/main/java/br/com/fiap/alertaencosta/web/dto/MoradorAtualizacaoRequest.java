package br.com.fiap.alertaencosta.web.dto;

import br.com.fiap.alertaencosta.domain.model.NecessidadeEspecial;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Substituicao completa (PUT) do cadastro. O CPF nao pode ser alterado.")
public record MoradorAtualizacaoRequest(
        @Schema(example = "Benedita Alves de Souza") @NotBlank @Size(max = 120) String nome,
        @Schema(example = "benedita.souza@email.com") @NotBlank @Email @Size(max = 150) String email,
        @Schema(example = "(11) 96543-0000") @NotBlank @Size(max = 20) String telefone,
        @Schema(example = "Viela da Paz, 42 - casa 3") @NotBlank @Size(max = 200) String endereco,
        @DecimalMin("-90") @DecimalMax("90") BigDecimal latitude,
        @DecimalMin("-180") @DecimalMax("180") BigDecimal longitude,
        @Schema(example = "5") @NotNull Long setorId,
        @Schema(example = "MOBILIDADE_REDUZIDA") NecessidadeEspecial necessidadeEspecial,
        @Size(max = 200) String pushToken
) {
}
