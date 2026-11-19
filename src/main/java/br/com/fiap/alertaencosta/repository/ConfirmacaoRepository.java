package br.com.fiap.alertaencosta.repository;

import br.com.fiap.alertaencosta.domain.model.ConfirmacaoAlerta;
import br.com.fiap.alertaencosta.domain.model.SituacaoMorador;
import br.com.fiap.alertaencosta.domain.model.StatusAlerta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfirmacaoRepository extends JpaRepository<ConfirmacaoAlerta, Long> {

    boolean existsByMoradorId(Long moradorId);

    long countByAlertaStatusAndSituacao(StatusAlerta status, SituacaoMorador situacao);
}
