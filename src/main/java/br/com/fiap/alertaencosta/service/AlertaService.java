package br.com.fiap.alertaencosta.service;

import br.com.fiap.alertaencosta.domain.exception.ConflitoException;
import br.com.fiap.alertaencosta.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.alertaencosta.domain.model.Alerta;
import br.com.fiap.alertaencosta.domain.model.ConfirmacaoAlerta;
import br.com.fiap.alertaencosta.domain.model.Morador;
import br.com.fiap.alertaencosta.domain.model.NivelRisco;
import br.com.fiap.alertaencosta.domain.model.OrigemAlerta;
import br.com.fiap.alertaencosta.domain.model.PontoApoio;
import br.com.fiap.alertaencosta.domain.model.Setor;
import br.com.fiap.alertaencosta.domain.model.SituacaoMorador;
import br.com.fiap.alertaencosta.domain.model.StatusAlerta;
import br.com.fiap.alertaencosta.repository.AlertaRepository;
import br.com.fiap.alertaencosta.repository.MoradorRepository;
import br.com.fiap.alertaencosta.web.dto.AlertaRequest;
import br.com.fiap.alertaencosta.web.dto.AlertaResponse;
import br.com.fiap.alertaencosta.web.dto.ConfirmacaoRequest;
import br.com.fiap.alertaencosta.web.dto.ConfirmacaoResponse;
import br.com.fiap.alertaencosta.web.dto.PaginaResponse;
import br.com.fiap.alertaencosta.web.dto.PendenciaResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Casos de uso do servico "Envio de alertas" e do apoio prioritario a vulneraveis. */
@Service
public class AlertaService {

    /** Resultado da aplicacao de um novo nivel de risco ao setor. */
    public record ResultadoAlerta(Alerta alerta, String acao) {
        static final ResultadoAlerta NENHUM = new ResultadoAlerta(null, "NENHUMA");
    }

    private final AlertaRepository repository;
    private final MoradorRepository moradorRepository;
    private final SetorService setorService;
    private final PontoApoioService pontoApoioService;
    private final MoradorService moradorService;
    private final NotificadorPush notificador;

    public AlertaService(AlertaRepository repository, MoradorRepository moradorRepository, SetorService setorService,
                         PontoApoioService pontoApoioService, MoradorService moradorService, NotificadorPush notificador) {
        this.repository = repository;
        this.moradorRepository = moradorRepository;
        this.setorService = setorService;
        this.pontoApoioService = pontoApoioService;
        this.moradorService = moradorService;
        this.notificador = notificador;
    }

    /**
     * Evento de negocio "Limite de risco atingido": risco ALTO emite alerta automatico
     * (ou escala o alerta MEDIO ja ativo). Niveis menores nao encerram alertas: isso e decisao da Defesa Civil.
     */
    @Transactional
    public ResultadoAlerta aplicarRisco(Setor setor, NivelRisco nivel) {
        if (nivel != NivelRisco.ALTO) {
            return ResultadoAlerta.NENHUM;
        }
        Optional<Alerta> ativo = repository.findFirstBySetorIdAndStatus(setor.getId(), StatusAlerta.ATIVO);
        if (ativo.isPresent()) {
            Alerta alerta = ativo.get();
            if (alerta.escalar(nivel)) {
                notificar(alerta);
                return new ResultadoAlerta(alerta, "ALERTA_ESCALADO");
            }
            return new ResultadoAlerta(alerta, "ALERTA_JA_ATIVO");
        }
        PontoApoio ponto = pontoApoioService.maisProximo(setor.getLatitude(), setor.getLongitude()).orElse(null);
        Alerta alerta = repository.save(Alerta.emitir(setor, nivel, ponto, OrigemAlerta.AUTOMATICO, null));
        notificar(alerta);
        return new ResultadoAlerta(alerta, "ALERTA_EMITIDO");
    }

    @Transactional
    public AlertaResponse emitirManual(AlertaRequest r) {
        Setor setor = setorService.buscarEntidade(r.setorId());
        repository.findFirstBySetorIdAndStatus(setor.getId(), StatusAlerta.ATIVO).ifPresent(a -> {
            throw new ConflitoException("O setor %s ja tem o alerta %d ATIVO. Encerre-o antes de emitir outro."
                    .formatted(setor.getCodigo(), a.getId()));
        });
        PontoApoio ponto = r.pontoApoioId() != null
                ? pontoApoioService.buscarEntidade(r.pontoApoioId())
                : pontoApoioService.maisProximo(setor.getLatitude(), setor.getLongitude()).orElse(null);
        Alerta alerta = repository.save(Alerta.emitir(setor, r.nivel(), ponto, OrigemAlerta.MANUAL, r.mensagem()));
        notificar(alerta);
        return AlertaResponse.de(alerta);
    }

    @Transactional(readOnly = true)
    public PaginaResponse<AlertaResponse> listar(Long setorId, StatusAlerta status, Pageable pageable) {
        var pagina = setorId != null && status != null ? repository.findBySetorIdAndStatus(setorId, status, pageable)
                : setorId != null ? repository.findBySetorId(setorId, pageable)
                : status != null ? repository.findByStatus(status, pageable)
                : repository.findAll(pageable);
        return PaginaResponse.de(pagina, AlertaResponse::de);
    }

    @Transactional(readOnly = true)
    public AlertaResponse buscar(Long id) {
        return AlertaResponse.de(buscarEntidade(id));
    }

    @Transactional
    public AlertaResponse encerrar(Long id, String motivo) {
        Alerta alerta = buscarEntidade(id);
        alerta.encerrar(motivo);
        return AlertaResponse.de(alerta);
    }

    @Transactional
    public ConfirmacaoResponse confirmar(Long alertaId, Long moradorId, ConfirmacaoRequest r) {
        Alerta alerta = buscarEntidade(alertaId);
        Morador morador = moradorService.buscarEntidade(moradorId);
        ConfirmacaoAlerta confirmacao = alerta.confirmar(morador, r.situacao(), r.observacao());
        repository.flush();
        return ConfirmacaoResponse.de(confirmacao);
    }

    @Transactional(readOnly = true)
    public List<ConfirmacaoResponse> confirmacoes(Long alertaId) {
        return buscarEntidade(alertaId).getConfirmacoes().stream()
                .sorted(Comparator.comparing(ConfirmacaoAlerta::getRegistradaEm))
                .map(ConfirmacaoResponse::de).toList();
    }

    /**
     * Busca ativa: moradores do setor que nao confirmaram estar seguros.
     * Ordem: quem pediu ajuda, depois prioritarios (idosos/PcD) sem resposta, depois os demais.
     */
    @Transactional(readOnly = true)
    public List<PendenciaResponse> pendencias(Long alertaId) {
        Alerta alerta = buscarEntidade(alertaId);
        return moradorRepository.findBySetorId(alerta.getSetor().getId()).stream()
                .map(m -> {
                    var conf = alerta.confirmacaoDe(m.getId());
                    if (conf.isPresent() && conf.get().getSituacao() == SituacaoMorador.SEGURO) {
                        return null;
                    }
                    String situacao = conf.isPresent() ? SituacaoMorador.PRECISA_AJUDA.name() : "SEM_RESPOSTA";
                    return new PendenciaResponse(m.getId(), m.getNome(), m.getTelefone(), m.getEndereco(),
                            m.getNecessidadeEspecial(), m.isPrioritario(), situacao,
                            conf.map(ConfirmacaoAlerta::getObservacao).orElse(null));
                })
                .filter(p -> p != null)
                .sorted(Comparator.comparing((PendenciaResponse p) -> !"PRECISA_AJUDA".equals(p.situacao()))
                        .thenComparing(p -> !p.prioritario())
                        .thenComparing(PendenciaResponse::nome))
                .toList();
    }

    private void notificar(Alerta alerta) {
        alerta.registrarNotificacao(notificador.notificar(alerta, moradorRepository.findBySetorId(alerta.getSetor().getId())));
    }

    private Alerta buscarEntidade(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Alerta", id));
    }
}
