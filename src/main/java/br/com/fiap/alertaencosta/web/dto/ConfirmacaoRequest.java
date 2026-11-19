package br.com.fiap.alertaencosta.web.dto;

import br.com.fiap.alertaencosta.domain.model.SituacaoMorador;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Resposta do morador ao alerta, enviada pelo app.")
public record ConfirmacaoRequest(
        @Schema(example = "PRECISA_AJUDA") @NotNull SituacaoMorador situacao,
        @Schema(example = "Cadeirante, preciso de ajuda para descer a escadaria.") @Size(max = 300) String observacao
) {
}
