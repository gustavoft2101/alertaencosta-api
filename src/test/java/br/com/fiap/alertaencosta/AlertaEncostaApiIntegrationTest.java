package br.com.fiap.alertaencosta;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sobe a aplicacao inteira com H2 em memoria: prova que as migracoes Flyway aplicam,
 * que o Hibernate aceita o schema (ddl-auto=validate) e que o fluxo principal funciona.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AlertaEncostaApiIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void cargaInicialDeixaOBr02EmRiscoAltoComAlertaAtivo() throws Exception {
        Long br02 = idSetor("BR-02");
        mvc.perform(get("/api/v1/setores/{id}/situacao", br02))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.setor.nivelRiscoAtual").value("ALTO"))
                .andExpect(jsonPath("$.alertaAtivo.nivel").value("ALTO"))
                .andExpect(jsonPath("$.moradoresPrioritarios").value(2));
    }

    @Test
    void leituraAcimaDoLimiteEmiteAlertaAutomaticoComRota() throws Exception {
        String estacao = mvc.perform(get("/api/v1/estacoes?setorId=" + idSetor("BR-01")))
                .andReturn().getResponse().getContentAsString();
        Long estacaoId = ((Number) JsonPath.read(estacao, "$.conteudo[0].id")).longValue();
        mvc.perform(post("/api/v1/estacoes/{id}/leituras", estacaoId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chuvaMm\": 95, \"umidadeSoloPct\": 92}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nivelAnterior").value("BAIXO"))
                .andExpect(jsonPath("$.nivelAtual").value("ALTO"))
                .andExpect(jsonPath("$.acao").value("ALERTA_EMITIDO"))
                .andExpect(jsonPath("$.alerta.totalNotificados").value(1));
    }

    @Test
    void pendenciasPriorizamQuemPediuAjuda() throws Exception {
        Long alerta = idAlertaAtivo(idSetor("BR-02"));
        mvc.perform(get("/api/v1/alertas/{id}/pendencias", alerta))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Maria da Silva"))
                .andExpect(jsonPath("$[0].situacao").value("PRECISA_AJUDA"))
                .andExpect(jsonPath("$[1].nome").value("Antonia Souza"))
                .andExpect(jsonPath("$[1].situacao").value("SEM_RESPOSTA"));
    }

    @Test
    void segundoAlertaNoMesmoSetorRetorna409() throws Exception {
        mvc.perform(post("/api/v1/alertas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"setorId\": %d, \"nivel\": \"ALTO\"}".formatted(idSetor("BR-02"))))
                .andExpect(status().isConflict());
    }

    @Test
    void validacaoRetorna400NoFormatoProblemDetail() throws Exception {
        mvc.perform(post("/api/v1/moradores").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Dados de entrada invalidos"))
                .andExpect(jsonPath("$.campos.nome").exists());
    }

    @Test
    void recursoInexistenteRetorna404() throws Exception {
        mvc.perform(get("/api/v1/setores/999999")).andExpect(status().isNotFound());
    }

    @Test
    void rotaSeguraIgnoraPontoInativo() throws Exception {
        mvc.perform(get("/api/v1/pontos-apoio/mais-proximo?latitude=-23.4430&longitude=-46.7080"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pontoApoio.situacao").value("ATIVO"))
                .andExpect(jsonPath("$.linkMapa").exists());
    }

    private Long idSetor(String codigo) throws Exception {
        String json = mvc.perform(get("/api/v1/setores?size=50")).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.<java.util.List<Object>>read(json, "$.conteudo[?(@.codigo == '" + codigo + "')].id").get(0)).longValue();
    }

    private Long idAlertaAtivo(Long setorId) throws Exception {
        String json = mvc.perform(get("/api/v1/alertas?status=ATIVO&setorId=" + setorId))
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(json, "$.conteudo[0].id")).longValue();
    }
}
