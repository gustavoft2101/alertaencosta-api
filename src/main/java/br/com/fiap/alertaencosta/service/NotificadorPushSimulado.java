package br.com.fiap.alertaencosta.service;

import br.com.fiap.alertaencosta.domain.model.Alerta;
import br.com.fiap.alertaencosta.domain.model.Morador;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Implementacao do prototipo: registra no log o push que seria enviado (dados simulados, restricao da GS/LGPD).
 * Notifica apenas moradores com token cadastrado; os demais aparecem nas pendencias para busca ativa.
 */
@Component
public class NotificadorPushSimulado implements NotificadorPush {

    private static final Logger log = LoggerFactory.getLogger(NotificadorPushSimulado.class);

    @Override
    public int notificar(Alerta alerta, List<Morador> moradores) {
        int enviados = 0;
        for (Morador m : moradores) {
            if (m.podeReceberPush()) {
                log.info("[PUSH simulado] alerta={} nivel={} morador={} token={}",
                        alerta.getId(), alerta.getNivel(), m.getId(), m.getPushToken());
                enviados++;
            }
        }
        log.info("[PUSH simulado] alerta={} setor={} notificados={}/{}",
                alerta.getId(), alerta.getSetor().getCodigo(), enviados, moradores.size());
        return enviados;
    }
}
