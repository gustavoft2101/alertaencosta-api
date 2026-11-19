package br.com.fiap.alertaencosta.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/** Metadados do contrato OpenAPI (code-first): http://localhost:8080/swagger-ui.html */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI alertaEncostaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("AlertaEncosta API")
                        .version("1.0.0")
                        .description("""
                                API REST central do AlertaEncosta (Global Solution 2026.2 - CitizenTech, FIAP).
                                Contrato unico usado pela estacao ESP32 (leituras), pelo modelo de ML (previsoes),
                                pelo app do morador (alerta, rota segura, confirmacao e reporte) e pelo painel
                                da Defesa Civil (setores, alertas, pendencias e ocorrencias).
                                Erros seguem a RFC 7807 (application/problem+json). Dados simulados (LGPD).""")
                        .contact(new Contact().name("Equipe AlertaEncosta - RM560239, RM559813, RM560271, RM550121, RM559616"))
                        .license(new License().name("Uso academico")))
                .servers(List.of(new Server().url("http://localhost:8080").description("Local")));
    }
}
