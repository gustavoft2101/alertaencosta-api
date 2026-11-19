package br.com.fiap.alertaencosta.web.controller;

import br.com.fiap.alertaencosta.service.EstacaoService;
import br.com.fiap.alertaencosta.service.MonitoramentoService;
import br.com.fiap.alertaencosta.web.dto.EstacaoAtualizacaoRequest;
import br.com.fiap.alertaencosta.web.dto.EstacaoRequest;
import br.com.fiap.alertaencosta.web.dto.EstacaoResponse;
import br.com.fiap.alertaencosta.web.dto.LeituraRequest;
import br.com.fiap.alertaencosta.web.dto.LeituraResponse;
import br.com.fiap.alertaencosta.web.dto.PaginaResponse;
import br.com.fiap.alertaencosta.web.dto.RiscoProcessadoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/estacoes")
@Tag(name = "Estacoes e leituras", description = "Estacoes ESP32 e a coleta de chuva, umidade do solo e nivel da agua")
public class EstacaoController {

    private final EstacaoService service;
    private final MonitoramentoService monitoramento;

    public EstacaoController(EstacaoService service, MonitoramentoService monitoramento) {
        this.service = service;
        this.monitoramento = monitoramento;
    }

    @PostMapping
    @Operation(summary = "Cadastrar estacao ESP32")
    @ApiResponse(responseCode = "201", description = "Estacao criada")
    @ApiResponse(responseCode = "400", description = "Dados invalidos")
    @ApiResponse(responseCode = "404", description = "Setor inexistente")
    @ApiResponse(responseCode = "409", description = "Codigo ja existente")
    public ResponseEntity<EstacaoResponse> cadastrar(@RequestBody @Valid EstacaoRequest req) {
        EstacaoResponse criada = service.cadastrar(req);
        return ResponseEntity.created(Uris.filho(criada.id())).body(criada);
    }

    @GetMapping
    @Operation(summary = "Listar estacoes", description = "Filtro opcional ?setorId=2.")
    public PaginaResponse<EstacaoResponse> listar(@RequestParam(required = false) Long setorId,
            @ParameterObject @PageableDefault(size = 20, sort = "codigo", direction = Sort.Direction.ASC) Pageable pageable) {
        return service.listar(setorId, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar estacao")
    @ApiResponse(responseCode = "200", description = "Estacao encontrada")
    @ApiResponse(responseCode = "404", description = "Estacao inexistente")
    public EstacaoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar estacao", description = "Use MANUTENCAO/INATIVA para bloquear o envio de leituras.")
    @ApiResponse(responseCode = "200", description = "Estacao atualizada")
    @ApiResponse(responseCode = "404", description = "Estacao ou setor inexistente")
    public EstacaoResponse atualizar(@PathVariable Long id, @RequestBody @Valid EstacaoAtualizacaoRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir estacao", description = "Bloqueado (409) se ja enviou leituras.")
    @ApiResponse(responseCode = "204", description = "Estacao excluida")
    @ApiResponse(responseCode = "404", description = "Estacao inexistente")
    @ApiResponse(responseCode = "409", description = "Estacao com historico de leituras")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/leituras")
    @Operation(summary = "Enviar leitura (firmware ESP32)",
            description = "Grava a leitura, classifica o risco pela regra de limites do setor e, se ALTO, emite o alerta com a rota segura.")
    @ApiResponse(responseCode = "201", description = "Leitura registrada e processada")
    @ApiResponse(responseCode = "400", description = "Valores fora da faixa do sensor")
    @ApiResponse(responseCode = "404", description = "Estacao inexistente")
    @ApiResponse(responseCode = "422", description = "Estacao fora de operacao ou horario no futuro")
    public ResponseEntity<RiscoProcessadoResponse> enviarLeitura(@PathVariable Long id, @RequestBody @Valid LeituraRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(monitoramento.registrarLeitura(id, req));
    }

    @GetMapping("/{id}/leituras")
    @Operation(summary = "Historico de leituras da estacao", description = "Mais recentes primeiro. Fonte de dados do modelo de ML e do painel.")
    @ApiResponse(responseCode = "200", description = "Leituras")
    @ApiResponse(responseCode = "404", description = "Estacao inexistente")
    public PaginaResponse<LeituraResponse> leituras(@PathVariable Long id,
            @ParameterObject @PageableDefault(size = 20, sort = "medidaEm", direction = Sort.Direction.DESC) Pageable pageable) {
        return monitoramento.leituras(id, pageable);
    }
}
