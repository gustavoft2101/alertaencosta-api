package br.com.fiap.alertaencosta.domain;

import br.com.fiap.alertaencosta.domain.exception.RegraNegocioException;
import br.com.fiap.alertaencosta.domain.model.Alerta;
import br.com.fiap.alertaencosta.domain.model.EstacaoMonitoramento;
import br.com.fiap.alertaencosta.domain.model.LeituraSensor;
import br.com.fiap.alertaencosta.domain.model.Morador;
import br.com.fiap.alertaencosta.domain.model.NecessidadeEspecial;
import br.com.fiap.alertaencosta.domain.model.NivelRisco;
import br.com.fiap.alertaencosta.domain.model.Ocorrencia;
import br.com.fiap.alertaencosta.domain.model.OrigemAlerta;
import br.com.fiap.alertaencosta.domain.model.PontoApoio;
import br.com.fiap.alertaencosta.domain.model.PrevisaoRisco;
import br.com.fiap.alertaencosta.domain.model.Setor;
import br.com.fiap.alertaencosta.domain.model.SituacaoEstacao;
import br.com.fiap.alertaencosta.domain.model.SituacaoMorador;
import br.com.fiap.alertaencosta.domain.model.SituacaoPontoApoio;
import br.com.fiap.alertaencosta.domain.model.StatusOcorrencia;
import br.com.fiap.alertaencosta.domain.model.TipoOcorrencia;
import br.com.fiap.alertaencosta.domain.model.TipoPontoApoio;
import br.com.fiap.alertaencosta.domain.model.TipoRisco;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Regras de negocio do dominio, sem Spring e sem banco. */
class DominioAlertaEncostaTest {

    private static BigDecimal d(String v) {
        return new BigDecimal(v);
    }

    private static Setor setor(long id, String codigo) {
        Setor s = new Setor(codigo, "Margem do Corrego", "Vila Brasilandia", TipoRisco.ALAGAMENTO,
                d("-23.4525"), d("-46.695"), d("60"), d("95"), d("150"));
        ReflectionTestUtils.setField(s, "id", id);
        return s;
    }

    private static PontoApoio ponto(SituacaoPontoApoio situacao) {
        return new PontoApoio("Paroquia", TipoPontoApoio.IGREJA, "Rua do Bananal, 950",
                d("-23.4495"), d("-46.6922"), 150, situacao);
    }

    private static Morador morador(long id, Setor setor) {
        Morador m = new Morador("Morador " + id, "m" + id + "@email.com", "0000000000" + id, "11", "Rua",
                null, null, setor, NecessidadeEspecial.IDOSO, null);
        ReflectionTestUtils.setField(m, "id", id);
        return m;
    }

    @ParameterizedTest(name = "chuva={0} solo={1} agua={2} -> {3}")
    @CsvSource({
            "30, 65, 70, BAIXO",    // tudo abaixo de 70% do limite
            "44, 90, 118, MEDIO",   // solo a 94,7%, mas chuva so a 73%
            "72, 97, 165, ALTO",    // chuva e nivel acima de 100%
            "52, 86, 10, ALTO"      // solo saturado: chuva 86,7% e solo 90,5% (ambos >= 85%)
    })
    void regraDeLimitesClassificaOSetor(String chuva, String solo, String agua, NivelRisco esperado) {
        EstacaoMonitoramento e = new EstacaoMonitoramento("ESP32-T", setor(1, "BR-02"), d("-23.45"), d("-46.69"), null);
        LeituraSensor leitura = e.registrarLeitura(d(chuva), d(solo), d(agua), null);
        assertThat(PrevisaoRisco.porRegraDeLimites(leitura).getNivel()).isEqualTo(esperado);
    }

    @Test
    void estacaoForaDeOperacaoOuRelogioNoFuturoNaoRegistraLeitura() {
        EstacaoMonitoramento e = new EstacaoMonitoramento("ESP32-T", setor(1, "BR-02"), d("-23.45"), d("-46.69"),
                SituacaoEstacao.MANUTENCAO);
        assertThatThrownBy(() -> e.registrarLeitura(d("1"), d("1"), null, null)).isInstanceOf(RegraNegocioException.class);
        e.atualizar(e.getSetor(), e.getLatitude(), e.getLongitude(), SituacaoEstacao.ATIVA);
        assertThatThrownBy(() -> e.registrarLeitura(d("1"), d("1"), null, LocalDateTime.now().plusHours(1)))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void alertaTemRotaEMensagemEPodeSerEscalado() {
        Alerta a = Alerta.emitir(setor(2, "BR-02"), NivelRisco.MEDIO, ponto(null), OrigemAlerta.AUTOMATICO, null);
        assertThat(a.getDistanciaMetros()).isEqualTo(439);
        assertThat(a.getMensagem()).startsWith("ATENCAO: risco MEDIO de alagamento").contains("439 m");
        assertThat(a.escalar(NivelRisco.ALTO)).isTrue();
        assertThat(a.getMensagem()).startsWith("ALERTA ALTO").contains("Defesa Civil 199");
        assertThat(a.escalar(NivelRisco.ALTO)).isFalse();
    }

    @Test
    void naoEmiteAlertaBaixoNemIndicaPontoInativo() {
        assertThatThrownBy(() -> Alerta.emitir(setor(2, "BR-02"), NivelRisco.BAIXO, ponto(null), OrigemAlerta.MANUAL, null))
                .isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> Alerta.emitir(setor(2, "BR-02"), NivelRisco.ALTO, ponto(SituacaoPontoApoio.INATIVO),
                OrigemAlerta.MANUAL, null)).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void confirmacaoEUnicaPorMoradorSoDoProprioSetorESoComAlertaAtivo() {
        Setor br02 = setor(2, "BR-02");
        Alerta a = Alerta.emitir(br02, NivelRisco.ALTO, ponto(null), OrigemAlerta.AUTOMATICO, null);
        Morador maria = morador(1, br02);
        a.confirmar(maria, SituacaoMorador.PRECISA_AJUDA, "Nao consigo descer");
        a.confirmar(maria, SituacaoMorador.SEGURO, "Resgatada");
        assertThat(a.getConfirmacoes()).hasSize(1);
        assertThat(a.contar(SituacaoMorador.SEGURO)).isEqualTo(1);

        Morador deOutroSetor = morador(9, setor(1, "BR-01"));
        assertThatThrownBy(() -> a.confirmar(deOutroSetor, SituacaoMorador.SEGURO, null))
                .isInstanceOf(RegraNegocioException.class);

        assertThatThrownBy(() -> a.encerrar(" ")).isInstanceOf(RegraNegocioException.class);
        a.encerrar("Chuva cessou");
        assertThatThrownBy(() -> a.confirmar(maria, SituacaoMorador.SEGURO, null)).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void ocorrenciaSegueOFluxoDeAtendimento() {
        Ocorrencia o = Ocorrencia.reportar(TipoOcorrencia.TRINCA_SOLO, "Rachadura no barranco", null,
                d("-23.45"), d("-46.69"), morador(1, setor(2, "BR-02")));
        assertThat(o.getProtocolo()).matches("AE-\\d{4}-[A-Z2-9]{6}");
        assertThat(o.getSetor().getCodigo()).isEqualTo("BR-02");
        assertThatThrownBy(() -> o.alterarStatus(StatusOcorrencia.RESOLVIDA, null)).isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> o.alterarStatus(StatusOcorrencia.DESCARTADA, null)).isInstanceOf(RegraNegocioException.class);
        o.alterarStatus(StatusOcorrencia.EM_VISTORIA, null);
        o.alterarStatus(StatusOcorrencia.RESOLVIDA, "Barranco contido");
        assertThat(o.getHistorico()).hasSize(3);
        assertThat(o.getResolvidoEm()).isNotNull();
        assertThatThrownBy(o::validarExclusao).isInstanceOf(RegraNegocioException.class);
    }
}
