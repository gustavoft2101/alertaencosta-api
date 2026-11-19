package br.com.fiap.alertaencosta.domain.model;

import java.math.BigDecimal;

/** Calculos geograficos usados na rota segura (distancia em linha reta pela formula de Haversine). */
public final class Geo {

    private static final double RAIO_TERRA_METROS = 6_371_000;
    /** Velocidade media de caminhada considerada na evacuacao (~4,2 km/h, ritmo de quem sai com criancas e idosos). */
    public static final double METROS_POR_MINUTO_A_PE = 70.0;

    private Geo() {
    }

    public static int distanciaMetros(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double dLon = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1.doubleValue())) * Math.cos(Math.toRadians(lat2.doubleValue()))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return (int) Math.round(RAIO_TERRA_METROS * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
    }

    public static int minutosAPe(int distanciaMetros) {
        return (int) Math.max(1, Math.ceil(distanciaMetros / METROS_POR_MINUTO_A_PE));
    }
}
