package br.com.fiap.alertaencosta.web.controller;

import br.com.fiap.alertaencosta.service.IndicadorService;
import br.com.fiap.alertaencosta.web.dto.IndicadoresResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/indicadores")
@Tag(name = "Indicadores", description = "Indicadores de impacto do piloto na Vila Brasilandia")
public class IndicadorController {

    private final IndicadorService service;

    public IndicadorController(IndicadorService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Painel de indicadores",
            description = "Setores por nivel, alertas ativos, % de alertados que confirmaram estar seguros, pedidos de ajuda, reportes e leituras.")
    public IndicadoresResponse indicadores() {
        return service.gerar();
    }
}
