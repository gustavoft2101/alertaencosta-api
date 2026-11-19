package br.com.fiap.alertaencosta.repository;

import br.com.fiap.alertaencosta.domain.model.PontoApoio;
import br.com.fiap.alertaencosta.domain.model.SituacaoPontoApoio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PontoApoioRepository extends JpaRepository<PontoApoio, Long> {

    List<PontoApoio> findBySituacao(SituacaoPontoApoio situacao);

    Page<PontoApoio> findBySituacao(SituacaoPontoApoio situacao, Pageable pageable);
}
