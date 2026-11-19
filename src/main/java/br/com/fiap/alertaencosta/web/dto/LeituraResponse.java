package br.com.fiap.alertaencosta.web.dto;

import br.com.fiap.alertaencosta.domain.model.LeituraSensor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LeituraResponse(
        Long id,
        Long estacaoId,
        String estacaoCodigo,
        BigDecimal chuvaMm,
        BigDecimal umidadeSoloPct,
        BigDecimal nivelAguaCm,
        LocalDateTime medidaEm,
        LocalDateTime recebidaEm
) {
    public static LeituraResponse de(LeituraSensor l) {
        return l == null ? null : new LeituraResponse(l.getId(), l.getEstacao().getId(), l.getEstacao().getCodigo(),
                l.getChuvaMm(), l.getUmidadeSoloPct(), l.getNivelAguaCm(), l.getMedidaEm(), l.getRecebidaEm());
    }
}
