package br.com.fiap.alertaencosta.service;

import br.com.fiap.alertaencosta.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.alertaencosta.domain.model.Ocorrencia;
import br.com.fiap.alertaencosta.domain.model.StatusOcorrencia;
import br.com.fiap.alertaencosta.domain.model.TipoOcorrencia;
import br.com.fiap.alertaencosta.repository.OcorrenciaRepository;
import br.com.fiap.alertaencosta.web.dto.HistoricoOcorrenciaResponse;
import br.com.fiap.alertaencosta.web.dto.OcorrenciaRequest;
import br.com.fiap.alertaencosta.web.dto.OcorrenciaResponse;
import br.com.fiap.alertaencosta.web.dto.PaginaResponse;
import br.com.fiap.alertaencosta.web.dto.StatusOcorrenciaRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static br.com.fiap.alertaencosta.repository.OcorrenciaSpecifications.comStatus;
import static br.com.fiap.alertaencosta.repository.OcorrenciaSpecifications.doMorador;
import static br.com.fiap.alertaencosta.repository.OcorrenciaSpecifications.doSetor;
import static br.com.fiap.alertaencosta.repository.OcorrenciaSpecifications.doTipo;

/** Servicos de negocio "Registro de ocorrencias" (morador) e "Atender ocorrencia" (Defesa Civil). */
@Service
public class OcorrenciaService {

    private final OcorrenciaRepository repository;
    private final MoradorService moradorService;

    public OcorrenciaService(OcorrenciaRepository repository, MoradorService moradorService) {
        this.repository = repository;
        this.moradorService = moradorService;
    }

    @Transactional
    public OcorrenciaResponse reportar(OcorrenciaRequest r) {
        var morador = moradorService.buscarEntidade(r.moradorId());
        return OcorrenciaResponse.de(repository.save(
                Ocorrencia.reportar(r.tipo(), r.descricao(), r.fotoUrl(), r.latitude(), r.longitude(), morador)));
    }

    @Transactional(readOnly = true)
    public PaginaResponse<OcorrenciaResponse> listar(StatusOcorrencia status, TipoOcorrencia tipo, Long setorId,
                                                     Long moradorId, Pageable pageable) {
        Specification<Ocorrencia> filtro = Specification.where(comStatus(status)).and(doTipo(tipo))
                .and(doSetor(setorId)).and(doMorador(moradorId));
        return PaginaResponse.de(repository.findAll(filtro, pageable), OcorrenciaResponse::de);
    }

    @Transactional(readOnly = true)
    public OcorrenciaResponse buscar(Long id) {
        return OcorrenciaResponse.de(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public OcorrenciaResponse buscarPorProtocolo(String protocolo) {
        return repository.findByProtocolo(protocolo.toUpperCase()).map(OcorrenciaResponse::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ocorrencia de protocolo", protocolo));
    }

    @Transactional
    public OcorrenciaResponse alterarStatus(Long id, StatusOcorrenciaRequest r) {
        Ocorrencia o = buscarEntidade(id);
        o.alterarStatus(r.status(), r.observacao());
        return OcorrenciaResponse.de(o);
    }

    @Transactional(readOnly = true)
    public List<HistoricoOcorrenciaResponse> historico(Long id) {
        return buscarEntidade(id).getHistorico().stream().map(HistoricoOcorrenciaResponse::de).toList();
    }

    @Transactional
    public void excluir(Long id) {
        Ocorrencia o = buscarEntidade(id);
        o.validarExclusao();
        repository.delete(o);
    }

    private Ocorrencia buscarEntidade(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Ocorrencia", id));
    }
}
