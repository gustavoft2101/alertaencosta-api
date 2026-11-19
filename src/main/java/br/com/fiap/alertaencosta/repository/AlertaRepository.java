package br.com.fiap.alertaencosta.repository;

import br.com.fiap.alertaencosta.domain.model.Alerta;
import br.com.fiap.alertaencosta.domain.model.StatusAlerta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    Optional<Alerta> findFirstBySetorIdAndStatus(Long setorId, StatusAlerta status);

    List<Alerta> findByStatus(StatusAlerta status);

    Page<Alerta> findByStatus(StatusAlerta status, Pageable pageable);

    Page<Alerta> findBySetorId(Long setorId, Pageable pageable);

    Page<Alerta> findBySetorIdAndStatus(Long setorId, StatusAlerta status, Pageable pageable);

    boolean existsBySetorId(Long setorId);

    boolean existsByPontoApoioId(Long pontoApoioId);

    long countByStatus(StatusAlerta status);
}
