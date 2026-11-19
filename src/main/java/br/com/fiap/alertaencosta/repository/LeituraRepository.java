package br.com.fiap.alertaencosta.repository;

import br.com.fiap.alertaencosta.domain.model.LeituraSensor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface LeituraRepository extends JpaRepository<LeituraSensor, Long> {

    Page<LeituraSensor> findByEstacaoId(Long estacaoId, Pageable pageable);

    boolean existsByEstacaoId(Long estacaoId);

    Optional<LeituraSensor> findFirstByEstacaoSetorIdOrderByMedidaEmDesc(Long setorId);

    long countByRecebidaEmAfter(LocalDateTime instante);
}
