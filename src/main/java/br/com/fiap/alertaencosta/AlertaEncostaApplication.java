package br.com.fiap.alertaencosta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AlertaEncosta API - CitizenTech da Global Solution 2026.2 (FIAP).
 * <p>
 * Recebe as leituras das estacoes ESP32 instaladas nas encostas da Vila Brasilandia,
 * classifica o risco de cada setor (regra de limites + previsoes do modelo de ML),
 * emite alertas com a rota a pe ate o ponto de apoio mais proximo, registra a
 * confirmacao "estou seguro / preciso de ajuda" dos moradores e os reportes de
 * trincas e agua subindo para a Defesa Civil.
 */
@SpringBootApplication
public class AlertaEncostaApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlertaEncostaApplication.class, args);
    }
}
