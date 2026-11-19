package br.com.fiap.alertaencosta.web.dto;

import br.com.fiap.alertaencosta.domain.model.Morador;
import br.com.fiap.alertaencosta.domain.model.NecessidadeEspecial;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Morador. CPF mascarado e token push nunca devolvido (LGPD: minimo necessario).")
public record MoradorResponse(
        @Schema(example = "7") Long id,
        @Schema(example = "Benedita Alves") String nome,
        String email,
        @Schema(example = "***.971.314-**") String cpf,
        String telefone,
        String endereco,
        BigDecimal latitude,
        BigDecimal longitude,
        Referencia setor,
        @Schema(example = "IDOSO") NecessidadeEspecial necessidadeEspecial,
        @Schema(example = "true") boolean prioritario,
        @Schema(example = "true", description = "Tem token push cadastrado.") boolean recebeNotificacao,
        LocalDateTime criadoEm
) {
    public static MoradorResponse de(Morador m) {
        return new MoradorResponse(m.getId(), m.getNome(), m.getEmail(), mascarar(m.getCpf()), m.getTelefone(),
                m.getEndereco(), m.getLatitude(), m.getLongitude(), SetorResponse.ref(m.getSetor()),
                m.getNecessidadeEspecial(), m.isPrioritario(), m.podeReceberPush(), m.getCriadoEm());
    }

    static String mascarar(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return cpf;
        }
        return "***." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-**";
    }
}
