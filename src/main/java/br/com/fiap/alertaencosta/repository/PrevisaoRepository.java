package br.com.fiap.alertaencosta.repository;

import br.com.fiap.alertaencosta.domain.model.PrevisaoRisco;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrevisaoRepository extends JpaRepository<PrevisaoRisco, Long> {

    Page<PrevisaoRisco> findBySetorId(Long setorId, Pageable pageable);

    boolean existsBySetorId(Long setorId);
}
