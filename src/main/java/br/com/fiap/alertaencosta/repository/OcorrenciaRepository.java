package br.com.fiap.alertaencosta.repository;

import br.com.fiap.alertaencosta.domain.model.Ocorrencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OcorrenciaRepository extends JpaRepository<Ocorrencia, Long>, JpaSpecificationExecutor<Ocorrencia> {

    Optional<Ocorrencia> findByProtocolo(String protocolo);

    boolean existsByMoradorId(Long moradorId);

    boolean existsBySetorId(Long setorId);

    @Query("select o.status, count(o) from Ocorrencia o group by o.status")
    List<Object[]> contarPorStatus();
}
