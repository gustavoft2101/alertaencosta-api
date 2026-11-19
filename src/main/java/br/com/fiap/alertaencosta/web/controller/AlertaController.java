package br.com.fiap.alertaencosta.web.controller;

import br.com.fiap.alertaencosta.domain.model.StatusAlerta;
import br.com.fiap.alertaencosta.service.AlertaService;
import br.com.fiap.alertaencosta.web.dto.AlertaRequest;
import br.com.fiap.alertaencosta.web.dto.AlertaResponse;
import br.com.fiap.alertaencosta.web.dto.ConfirmacaoRequest;
import br.com.fiap.alertaencosta.web.dto.ConfirmacaoResponse;
import br.com.fiap.alertaencosta.web.dto.EncerramentoRequest;
import br.com.fiap.alertaencosta.web.dto.PaginaResponse;
import br.com.fiap.alertaencosta.web.dto.PendenciaResponse;
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
@RequestMapping("/api/v1/alertas")
@Tag(name = "Alertas", description = "Emissao, confirmacao dos moradores, busca ativa de vulneraveis e encerramento")
public class AlertaController {

    private final AlertaService service;

    public AlertaController(AlertaService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Emitir alerta manual (Defesa Civil)",
            description = "Nivel MEDIO ou ALTO. Sem pontoApoioId, usa o ponto ativo mais proximo. Um alerta ATIVO por setor.")
    @ApiResponse(responseCode = "201", description = "Alerta emitido e moradores notificados")
    @ApiResponse(responseCode = "400", description = "Dados invalidos")
    @ApiResponse(responseCode = "404", description = "Setor ou ponto inexistente")
    @ApiResponse(responseCode = "409", description = "Setor ja tem alerta ativo")
    @ApiResponse(responseCode = "422", description = "Nivel BAIXO ou ponto de apoio inativo")
    public ResponseEntity<AlertaResponse> emitir(@RequestBody @Valid AlertaRequest req) {
        AlertaResponse criado = service.emitirManual(req);
        return ResponseEntity.created(Uris.filho(criado.id())).body(criado);
    }

    @GetMapping
    @Operation(summary = "Listar alertas", description = "Filtros opcionais ?status=ATIVO&setorId=2.")
    public PaginaResponse<AlertaResponse> listar(@RequestParam(required = false) Long setorId,
            @RequestParam(required = false) StatusAlerta status,
            @ParameterObject @PageableDefault(size = 10, sort = "emitidoEm", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.listar(setorId, status, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar alerta", description = "Inclui mensagem, rota, notificados e contagem de seguros/pedidos de ajuda.")
    @ApiResponse(responseCode = "200", description = "Alerta encontrado")
    @ApiResponse(responseCode = "404", description = "Alerta inexistente")
    public AlertaResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PatchMapping("/{id}/encerramento")
    @Operation(summary = "Encerrar alerta", description = "Decisao da Defesa Civil, com motivo obrigatorio. ENCERRADO e terminal.")
    @ApiResponse(responseCode = "200", description = "Alerta encerrado")
    @ApiResponse(responseCode = "400", description = "Motivo ausente")
    @ApiResponse(responseCode = "404", description = "Alerta inexistente")
    @ApiResponse(responseCode = "422", description = "Alerta ja encerrado")
    public AlertaResponse encerrar(@PathVariable Long id, @RequestBody @Valid EncerramentoRequest req) {
        return service.encerrar(id, req.motivo());
    }

    @PutMapping("/{id}/confirmacoes/{moradorId}")
    @Operation(summary = "Morador responde ao alerta (estou seguro / preciso de ajuda)",
            description = "PUT porque e idempotente: uma resposta por morador, que pode ser atualizada (ex.: PRECISA_AJUDA -> SEGURO apos o resgate).")
    @ApiResponse(responseCode = "200", description = "Resposta registrada")
    @ApiResponse(responseCode = "404", description = "Alerta ou morador inexistente")
    @ApiResponse(responseCode = "422", description = "Alerta encerrado ou morador de outro setor")
    public ConfirmacaoResponse confirmar(@PathVariable Long id, @PathVariable Long moradorId,
                                         @RequestBody @Valid ConfirmacaoRequest req) {
        return service.confirmar(id, moradorId, req);
    }

    @GetMapping("/{id}/confirmacoes")
    @Operation(summary = "Respostas dos moradores ao alerta")
    @ApiResponse(responseCode = "200", description = "Confirmacoes")
    @ApiResponse(responseCode = "404", description = "Alerta inexistente")
    public List<ConfirmacaoResponse> confirmacoes(@PathVariable Long id) {
        return service.confirmacoes(id);
    }

    @GetMapping("/{id}/pendencias")
    @Operation(summary = "Busca ativa: quem ainda nao esta seguro",
            description = "Primeiro quem pediu ajuda, depois idosos e PcD sem resposta, depois os demais sem resposta.")
    @ApiResponse(responseCode = "200", description = "Pendencias")
    @ApiResponse(responseCode = "404", description = "Alerta inexistente")
    public List<PendenciaResponse> pendencias(@PathVariable Long id) {
        return service.pendencias(id);
    }
}
