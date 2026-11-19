package br.com.fiap.alertaencosta.web.dto;

import br.com.fiap.alertaencosta.domain.model.Geo;
import br.com.fiap.alertaencosta.domain.model.PontoApoio;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Rota a pe ate o ponto de apoio ativo mais proximo.")
public record RotaSeguraResponse(
        BigDecimal origemLatitude,
        BigDecimal origemLongitude,
        PontoApoioResponse pontoApoio,
        @Schema(example = "438", description = "Distancia em linha reta (Haversine).") int distanciaMetros,
        @Schema(example = "7", description = "Tempo estimado a pe (70 m/min).") int tempoEstimadoMinutos,
        @Schema(example = "https://www.google.com/maps/dir/?api=1&origin=-23.4525,-46.695&destination=-23.4495,-46.6922&travelmode=walking")
        String linkMapa
) {
    public static RotaSeguraResponse de(BigDecimal lat, BigDecimal lon, PontoApoio ponto) {
        int distancia = ponto.distanciaMetrosAte(lat, lon);
        return new RotaSeguraResponse(lat, lon, PontoApoioResponse.de(ponto), distancia, Geo.minutosAPe(distancia),
                linkMapa(lat, lon, ponto));
    }

    public static String linkMapa(BigDecimal lat, BigDecimal lon, PontoApoio ponto) {
        return "https://www.google.com/maps/dir/?api=1&origin=%s,%s&destination=%s,%s&travelmode=walking"
                .formatted(lat.toPlainString(), lon.toPlainString(),
                        ponto.getLatitude().toPlainString(), ponto.getLongitude().toPlainString());
    }
}
