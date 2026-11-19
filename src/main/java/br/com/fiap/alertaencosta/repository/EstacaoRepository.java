package br.com.fiap.alertaencosta.repository;

import br.com.fiap.alertaencosta.domain.model.EstacaoMonitoramento;
import br.com.fiap.alertaencosta.domain.model.SituacaoEstacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstacaoRepository extends JpaRepository<EstacaoMonitoramento, Long> {

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsBySetorId(Long setorId);

    long countBySetorIdAndSituacao(Long setorId, SituacaoEstacao situacao);

    Page<EstacaoMonitoramento> findBySetorId(Long setorId, Pageable pageable);
}
