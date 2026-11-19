package br.com.fiap.alertaencosta.service;

import br.com.fiap.alertaencosta.domain.model.NecessidadeEspecial;
import br.com.fiap.alertaencosta.domain.model.NivelRisco;
import br.com.fiap.alertaencosta.domain.model.SituacaoMorador;
import br.com.fiap.alertaencosta.domain.model.StatusAlerta;
import br.com.fiap.alertaencosta.domain.model.StatusOcorrencia;
import br.com.fiap.alertaencosta.repository.AlertaRepository;
import br.com.fiap.alertaencosta.repository.ConfirmacaoRepository;
import br.com.fiap.alertaencosta.repository.LeituraRepository;
import br.com.fiap.alertaencosta.repository.MoradorRepository;
import br.com.fiap.alertaencosta.repository.OcorrenciaRepository;
import br.com.fiap.alertaencosta.repository.SetorRepository;
import br.com.fiap.alertaencosta.web.dto.IndicadoresResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class IndicadorService {

    private final SetorRepository setorRepository;
    private final AlertaRepository alertaRepository;
    private final MoradorRepository moradorRepository;
    private final ConfirmacaoRepository confirmacaoRepository;
    private final OcorrenciaRepository ocorrenciaRepository;
    private final LeituraRepository leituraRepository;

    public IndicadorService(SetorRepository setorRepository, AlertaRepository alertaRepository,
                            MoradorRepository moradorRepository, ConfirmacaoRepository confirmacaoRepository,
                            OcorrenciaRepository ocorrenciaRepository, LeituraRepository leituraRepository) {
        this.setorRepository = setorRepository;
        this.alertaRepository = alertaRepository;
        this.moradorRepository = moradorRepository;
        this.confirmacaoRepository = confirmacaoRepository;
        this.ocorrenciaRepository = ocorrenciaRepository;
        this.leituraRepository = leituraRepository;
    }

    @Transactional(readOnly = true)
    public IndicadoresResponse gerar() {
        long alertados = moradorRepository.contarEmSetoresComAlerta(StatusAlerta.ATIVO);
        long seguros = confirmacaoRepository.countByAlertaStatusAndSituacao(StatusAlerta.ATIVO, SituacaoMorador.SEGURO);
        BigDecimal percentual = alertados == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(seguros * 100).divide(BigDecimal.valueOf(alertados), 2, RoundingMode.HALF_UP);
        return new IndicadoresResponse(
                contagem(NivelRisco.values(), setorRepository.contarPorNivel()),
                alertaRepository.countByStatus(StatusAlerta.ATIVO),
                moradorRepository.count(),
                moradorRepository.countByNecessidadeEspecialNot(NecessidadeEspecial.NENHUMA),
                alertados,
                seguros,
                confirmacaoRepository.countByAlertaStatusAndSituacao(StatusAlerta.ATIVO, SituacaoMorador.PRECISA_AJUDA),
                percentual,
                contagem(StatusOcorrencia.values(), ocorrenciaRepository.contarPorStatus()),
                leituraRepository.countByRecebidaEmAfter(LocalDateTime.now().minusHours(24)));
    }

    /** Todos os valores do enum aparecem no mapa, mesmo com zero. */
    private static Map<String, Long> contagem(Enum<?>[] valores, List<Object[]> linhas) {
        Map<String, Long> mapa = new LinkedHashMap<>();
        for (Enum<?> v : valores) {
            mapa.put(v.name(), 0L);
        }
        for (Object[] l : linhas) {
            mapa.put(((Enum<?>) l[0]).name(), ((Number) l[1]).longValue());
        }
        return mapa;
    }
}
