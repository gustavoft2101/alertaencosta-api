package br.com.fiap.alertaencosta.repository;

import br.com.fiap.alertaencosta.domain.model.Morador;
import br.com.fiap.alertaencosta.domain.model.NecessidadeEspecial;
import br.com.fiap.alertaencosta.domain.model.StatusAlerta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MoradorRepository extends JpaRepository<Morador, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByCpf(String cpf);

    boolean existsBySetorId(Long setorId);

    List<Morador> findBySetorId(Long setorId);

    Page<Morador> findBySetorId(Long setorId, Pageable pageable);

    long countBySetorId(Long setorId);

    long countBySetorIdAndNecessidadeEspecialNot(Long setorId, NecessidadeEspecial necessidade);

    long countByNecessidadeEspecialNot(NecessidadeEspecial necessidade);

    /** Moradores que estao em setores com alerta ativo (base do % "confirmaram estar seguros"). */
    @Query("""
            select count(m) from Morador m
            where m.setor.id in (select a.setor.id from Alerta a where a.status = :status)
            """)
    long contarEmSetoresComAlerta(@Param("status") StatusAlerta status);
}
