package br.com.fiap.alertaencosta.web.dto;

import br.com.fiap.alertaencosta.domain.model.NecessidadeEspecial;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Cadastro do morador, ligado ao setor de risco da sua casa.")
public record MoradorRequest(
        @Schema(example = "Benedita Alves") @NotBlank @Size(max = 120) String nome,
        @Schema(example = "benedita.alves@email.com") @NotBlank @Email @Size(max = 150) String email,
        @Schema(example = "746.971.314-01", description = "Com ou sem mascara. Unico e imutavel.")
        @NotBlank @Pattern(regexp = "^\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}$", message = "CPF deve ter 11 digitos (com ou sem mascara)") String cpf,
        @Schema(example = "(11) 96543-2109") @NotBlank @Size(max = 20) String telefone,
        @Schema(example = "Viela da Paz, 42 - casa 3") @NotBlank @Size(max = 200) String endereco,
        @Schema(example = "-23.448300", description = "Opcional; sem ela a rota parte do centro do setor.")
        @DecimalMin("-90") @DecimalMax("90") BigDecimal latitude,
        @Schema(example = "-46.699400") @DecimalMin("-180") @DecimalMax("180") BigDecimal longitude,
        @Schema(example = "5") @NotNull Long setorId,
        @Schema(example = "IDOSO", description = "Opcional, padrao NENHUMA. Diferente de NENHUMA = prioridade na busca ativa.")
        NecessidadeEspecial necessidadeEspecial,
        @Schema(example = "ExponentPushToken[simulado-benedita]", description = "Token Expo Push do celular (opcional).")
        @Size(max = 200) String pushToken
) {
}
