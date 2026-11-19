package br.com.fiap.alertaencosta.repository;

import br.com.fiap.alertaencosta.domain.model.Ocorrencia;
import br.com.fiap.alertaencosta.domain.model.StatusOcorrencia;
import br.com.fiap.alertaencosta.domain.model.TipoOcorrencia;
import org.springframework.data.jpa.domain.Specification;

/** Filtros opcionais e combinaveis da listagem de reportes. */
public final class OcorrenciaSpecifications {

    private OcorrenciaSpecifications() {
    }

    public static Specification<Ocorrencia> comStatus(StatusOcorrencia status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Ocorrencia> doTipo(TipoOcorrencia tipo) {
        return (root, query, cb) -> tipo == null ? null : cb.equal(root.get("tipo"), tipo);
    }

    public static Specification<Ocorrencia> doSetor(Long setorId) {
        return (root, query, cb) -> setorId == null ? null : cb.equal(root.get("setor").get("id"), setorId);
    }

    public static Specification<Ocorrencia> doMorador(Long moradorId) {
        return (root, query, cb) -> moradorId == null ? null : cb.equal(root.get("morador").get("id"), moradorId);
    }
}
