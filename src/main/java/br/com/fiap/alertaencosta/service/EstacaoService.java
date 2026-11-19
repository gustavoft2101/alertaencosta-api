package br.com.fiap.alertaencosta.service;

import br.com.fiap.alertaencosta.domain.exception.ConflitoException;
import br.com.fiap.alertaencosta.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.alertaencosta.domain.model.EstacaoMonitoramento;
import br.com.fiap.alertaencosta.repository.EstacaoRepository;
import br.com.fiap.alertaencosta.repository.LeituraRepository;
import br.com.fiap.alertaencosta.web.dto.EstacaoAtualizacaoRequest;
import br.com.fiap.alertaencosta.web.dto.EstacaoRequest;
import br.com.fiap.alertaencosta.web.dto.EstacaoResponse;
import br.com.fiap.alertaencosta.web.dto.PaginaResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EstacaoService {

    private final EstacaoRepository repository;
    private final LeituraRepository leituraRepository;
    private final SetorService setorService;

    public EstacaoService(EstacaoRepository repository, LeituraRepository leituraRepository, SetorService setorService) {
        this.repository = repository;
        this.leituraRepository = leituraRepository;
        this.setorService = setorService;
    }

    @Transactional
    public EstacaoResponse cadastrar(EstacaoRequest r) {
        if (repository.existsByCodigoIgnoreCase(r.codigo())) {
            throw new ConflitoException("Ja existe estacao com o codigo " + r.codigo().toUpperCase() + ".");
        }
        var setor = setorService.buscarEntidade(r.setorId());
        return EstacaoResponse.de(repository.save(
                new EstacaoMonitoramento(r.codigo(), setor, r.latitude(), r.longitude(), r.situacao())));
    }

    @Transactional(readOnly = true)
    public PaginaResponse<EstacaoResponse> listar(Long setorId, Pageable pageable) {
        var pagina = setorId == null ? repository.findAll(pageable) : repository.findBySetorId(setorId, pageable);
        return PaginaResponse.de(pagina, EstacaoResponse::de);
    }

    @Transactional(readOnly = true)
    public EstacaoResponse buscar(Long id) {
        return EstacaoResponse.de(buscarEntidade(id));
    }

    @Transactional
    public EstacaoResponse atualizar(Long id, EstacaoAtualizacaoRequest r) {
        EstacaoMonitoramento estacao = buscarEntidade(id);
        estacao.atualizar(setorService.buscarEntidade(r.setorId()), r.latitude(), r.longitude(), r.situacao());
        return EstacaoResponse.de(estacao);
    }

    @Transactional
    public void excluir(Long id) {
        EstacaoMonitoramento estacao = buscarEntidade(id);
        if (leituraRepository.existsByEstacaoId(id)) {
            throw new ConflitoException("A estacao %s ja enviou leituras (historico usado pelo modelo). Use situacao INATIVA."
                    .formatted(estacao.getCodigo()));
        }
        repository.delete(estacao);
    }

    EstacaoMonitoramento buscarEntidade(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Estacao", id));
    }
}
