package br.com.fiap.alertaencosta.service;

import br.com.fiap.alertaencosta.domain.exception.ConflitoException;
import br.com.fiap.alertaencosta.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.alertaencosta.domain.model.NivelRisco;
import br.com.fiap.alertaencosta.domain.model.Setor;
import br.com.fiap.alertaencosta.domain.model.SituacaoEstacao;
import br.com.fiap.alertaencosta.domain.model.StatusAlerta;
import br.com.fiap.alertaencosta.domain.model.NecessidadeEspecial;
import br.com.fiap.alertaencosta.repository.AlertaRepository;
import br.com.fiap.alertaencosta.repository.EstacaoRepository;
import br.com.fiap.alertaencosta.repository.LeituraRepository;
import br.com.fiap.alertaencosta.repository.MoradorRepository;
import br.com.fiap.alertaencosta.repository.OcorrenciaRepository;
import br.com.fiap.alertaencosta.repository.PrevisaoRepository;
import br.com.fiap.alertaencosta.repository.SetorRepository;
import br.com.fiap.alertaencosta.web.dto.AlertaResumo;
import br.com.fiap.alertaencosta.web.dto.LeituraResponse;
import br.com.fiap.alertaencosta.web.dto.PaginaResponse;
import br.com.fiap.alertaencosta.web.dto.SetorAtualizacaoRequest;
import br.com.fiap.alertaencosta.web.dto.SetorRequest;
import br.com.fiap.alertaencosta.web.dto.SetorResponse;
import br.com.fiap.alertaencosta.web.dto.SetorSituacaoResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SetorService {

    private final SetorRepository repository;
    private final MoradorRepository moradorRepository;
    private final EstacaoRepository estacaoRepository;
    private final LeituraRepository leituraRepository;
    private final PrevisaoRepository previsaoRepository;
    private final AlertaRepository alertaRepository;
    private final OcorrenciaRepository ocorrenciaRepository;

    public SetorService(SetorRepository repository, MoradorRepository moradorRepository,
                        EstacaoRepository estacaoRepository, LeituraRepository leituraRepository,
                        PrevisaoRepository previsaoRepository, AlertaRepository alertaRepository,
                        OcorrenciaRepository ocorrenciaRepository) {
        this.repository = repository;
        this.moradorRepository = moradorRepository;
        this.estacaoRepository = estacaoRepository;
        this.leituraRepository = leituraRepository;
        this.previsaoRepository = previsaoRepository;
        this.alertaRepository = alertaRepository;
        this.ocorrenciaRepository = ocorrenciaRepository;
    }

    @Transactional
    public SetorResponse cadastrar(SetorRequest r) {
        if (repository.existsByCodigoIgnoreCase(r.codigo())) {
            throw new ConflitoException("Ja existe setor com o codigo " + r.codigo().toUpperCase() + ".");
        }
        Setor setor = new Setor(r.codigo(), r.nome(), r.bairro(), r.tipoRisco(), r.latitude(), r.longitude(),
                r.limiteChuvaMm(), r.limiteUmidadeSoloPct(), r.limiteNivelAguaCm());
        return SetorResponse.de(repository.save(setor));
    }

    @Transactional(readOnly = true)
    public PaginaResponse<SetorResponse> listar(NivelRisco nivel, Pageable pageable) {
        var pagina = nivel == null ? repository.findAll(pageable) : repository.findByNivelRiscoAtual(nivel, pageable);
        return PaginaResponse.de(pagina, SetorResponse::de);
    }

    @Transactional(readOnly = true)
    public SetorResponse buscar(Long id) {
        return SetorResponse.de(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public SetorSituacaoResponse situacao(Long id) {
        Setor setor = buscarEntidade(id);
        return new SetorSituacaoResponse(
                SetorResponse.de(setor),
                leituraRepository.findFirstByEstacaoSetorIdOrderByMedidaEmDesc(id).map(LeituraResponse::de).orElse(null),
                alertaRepository.findFirstBySetorIdAndStatus(id, StatusAlerta.ATIVO).map(AlertaResumo::de).orElse(null),
                moradorRepository.countBySetorId(id),
                moradorRepository.countBySetorIdAndNecessidadeEspecialNot(id, NecessidadeEspecial.NENHUMA),
                estacaoRepository.countBySetorIdAndSituacao(id, SituacaoEstacao.ATIVA));
    }

    @Transactional
    public SetorResponse atualizar(Long id, SetorAtualizacaoRequest r) {
        Setor setor = buscarEntidade(id);
        setor.atualizar(r.nome(), r.bairro(), r.tipoRisco(), r.latitude(), r.longitude(),
                r.limiteChuvaMm(), r.limiteUmidadeSoloPct(), r.limiteNivelAguaCm());
        return SetorResponse.de(setor);
    }

    @Transactional
    public void excluir(Long id) {
        Setor setor = buscarEntidade(id);
        if (moradorRepository.existsBySetorId(id) || estacaoRepository.existsBySetorId(id)
                || alertaRepository.existsBySetorId(id) || ocorrenciaRepository.existsBySetorId(id)
                || previsaoRepository.existsBySetorId(id)) {
            throw new ConflitoException("O setor %s possui moradores, estacoes, alertas, previsoes ou reportes e nao pode ser excluido."
                    .formatted(setor.getCodigo()));
        }
        repository.delete(setor);
    }

    Setor buscarEntidade(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Setor", id));
    }
}
