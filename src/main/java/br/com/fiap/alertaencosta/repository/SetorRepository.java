package br.com.fiap.alertaencosta.repository;

import br.com.fiap.alertaencosta.domain.model.NivelRisco;
import br.com.fiap.alertaencosta.domain.model.Setor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SetorRepository extends JpaRepository<Setor, Long> {

    Page<Setor> findByNivelRiscoAtual(NivelRisco nivel, Pageable pageable);

    boolean existsByCodigoIgnoreCase(String codigo);

    @Query("select s.nivelRiscoAtual, count(s) from Setor s group by s.nivelRiscoAtual")
    List<Object[]> contarPorNivel();
}
