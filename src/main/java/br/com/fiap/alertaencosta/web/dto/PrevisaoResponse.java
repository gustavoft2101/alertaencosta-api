package br.com.fiap.alertaencosta.web.dto;

import br.com.fiap.alertaencosta.domain.model.NivelRisco;
import br.com.fiap.alertaencosta.domain.model.OrigemPrevisao;
import br.com.fiap.alertaencosta.domain.model.PrevisaoRisco;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PrevisaoResponse(
        Long id,
        Referencia setor,
        NivelRisco nivel,
        BigDecimal probabilidade,
        OrigemPrevisao origem,
        String versaoModelo,
        Long leituraId,
        LocalDateTime criadaEm
) {
    public static PrevisaoResponse de(PrevisaoRisco p) {
        return new PrevisaoResponse(p.getId(), SetorResponse.ref(p.getSetor()), p.getNivel(), p.getProbabilidade(),
                p.getOrigem(), p.getVersaoModelo(), p.getLeitura() == null ? null : p.getLeitura().getId(),
                p.getCriadaEm());
    }
}
