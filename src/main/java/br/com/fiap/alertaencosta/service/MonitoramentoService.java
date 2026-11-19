package br.com.fiap.alertaencosta.service;

import br.com.fiap.alertaencosta.domain.model.EstacaoMonitoramento;
import br.com.fiap.alertaencosta.domain.model.LeituraSensor;
import br.com.fiap.alertaencosta.domain.model.NivelRisco;
import br.com.fiap.alertaencosta.domain.model.PrevisaoRisco;
import br.com.fiap.alertaencosta.domain.model.Setor;
import br.com.fiap.alertaencosta.repository.LeituraRepository;
import br.com.fiap.alertaencosta.repository.PrevisaoRepository;
import br.com.fiap.alertaencosta.web.dto.AlertaResumo;
import br.com.fiap.alertaencosta.web.dto.LeituraRequest;
import br.com.fiap.alertaencosta.web.dto.LeituraResponse;
import br.com.fiap.alertaencosta.web.dto.PaginaResponse;
import br.com.fiap.alertaencosta.web.dto.PrevisaoRequest;
import br.com.fiap.alertaencosta.web.dto.PrevisaoResponse;
import br.com.fiap.alertaencosta.web.dto.RiscoProcessadoResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicos "Coleta de leituras de sensores" e "Previsao de risco".
 * Fluxo: leitura do ESP32 -> classificacao pela regra de limites -> novo nivel do setor -> alerta (se ALTO).
 * O modelo de ML (Python) publica previsoes pelo mesmo fluxo; o nivel do setor e sempre o da classificacao mais recente.
 */
@Service
public class MonitoramentoService {

    private final LeituraRepository leituraRepository;
    private final PrevisaoRepository previsaoRepository;
    private final EstacaoService estacaoService;
    private final SetorService setorService;
    private final AlertaService alertaService;

    public MonitoramentoService(LeituraRepository leituraRepository, PrevisaoRepository previsaoRepository,
                                EstacaoService estacaoService, SetorService setorService, AlertaService alertaService) {
        this.leituraRepository = leituraRepository;
        this.previsaoRepository = previsaoRepository;
        this.estacaoService = estacaoService;
        this.setorService = setorService;
        this.alertaService = alertaService;
    }

    @Transactional
    public RiscoProcessadoResponse registrarLeitura(Long estacaoId, LeituraRequest r) {
        EstacaoMonitoramento estacao = estacaoService.buscarEntidade(estacaoId);
        LeituraSensor leitura = leituraRepository.save(
                estacao.registrarLeitura(r.chuvaMm(), r.umidadeSoloPct(), r.nivelAguaCm(), r.medidaEm()));
        PrevisaoRisco previsao = previsaoRepository.save(PrevisaoRisco.porRegraDeLimites(leitura));
        return processar(estacao.getSetor(), previsao, LeituraResponse.de(leitura));
    }

    @Transactional
    public RiscoProcessadoResponse registrarPrevisao(Long setorId, PrevisaoRequest r) {
        Setor setor = setorService.buscarEntidade(setorId);
        PrevisaoRisco previsao = previsaoRepository.save(
                PrevisaoRisco.doModelo(setor, r.nivel(), r.probabilidade(), r.versaoModelo()));
        return processar(setor, previsao, null);
    }

    @Transactional(readOnly = true)
    public PaginaResponse<LeituraResponse> leituras(Long estacaoId, Pageable pageable) {
        estacaoService.buscarEntidade(estacaoId);
        return PaginaResponse.de(leituraRepository.findByEstacaoId(estacaoId, pageable), LeituraResponse::de);
    }

    @Transactional(readOnly = true)
    public PaginaResponse<PrevisaoResponse> previsoes(Long setorId, Pageable pageable) {
        setorService.buscarEntidade(setorId);
        return PaginaResponse.de(previsaoRepository.findBySetorId(setorId, pageable), PrevisaoResponse::de);
    }

    private RiscoProcessadoResponse processar(Setor setor, PrevisaoRisco previsao, LeituraResponse leitura) {
        NivelRisco anterior = setor.atualizarRisco(previsao.getNivel());
        AlertaService.ResultadoAlerta resultado = alertaService.aplicarRisco(setor, previsao.getNivel());
        return new RiscoProcessadoResponse(leitura, PrevisaoResponse.de(previsao), anterior, setor.getNivelRiscoAtual(),
                AlertaResumo.de(resultado.alerta()), resultado.acao());
    }
}
