package br.com.fiap.alertaencosta.service;

import br.com.fiap.alertaencosta.domain.model.Alerta;
import br.com.fiap.alertaencosta.domain.model.Morador;

import java.util.List;

/**
 * Fronteira com o servico externo de notificacao (Expo Push, na camada de Tecnologia da arquitetura).
 * A API depende deste contrato, nao da implementacao: trocar o simulador pelo cliente HTTP do Expo
 * nao muda nenhuma regra de negocio.
 */
public interface NotificadorPush {

    /** @return quantos moradores foram efetivamente notificados */
    int notificar(Alerta alerta, List<Morador> moradores);
}
