package br.com.fiap.alertaencosta.web.controller;

import br.com.fiap.alertaencosta.domain.model.StatusOcorrencia;
import br.com.fiap.alertaencosta.domain.model.TipoOcorrencia;
import br.com.fiap.alertaencosta.service.OcorrenciaService;
import br.com.fiap.alertaencosta.web.dto.HistoricoOcorrenciaResponse;
import br.com.fiap.alertaencosta.web.dto.OcorrenciaRequest;
import br.com.fiap.alertaencosta.web.dto.OcorrenciaResponse;
import br.com.fiap.alertaencosta.web.dto.PaginaResponse;
import br.com.fiap.alertaencosta.web.dto.StatusOcorrenciaRequest;
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
@RequestMapping("/api/v1/ocorrencias")
@Tag(name = "Ocorrencias", description = "Reportes de trincas e agua subindo, com foto e GPS, e seu atendimento")
public class OcorrenciaController {

    private final OcorrenciaService service;

    public OcorrenciaController(OcorrenciaService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Reportar ocorrencia (app do morador)", description = "Gera protocolo publico e vincula ao setor do morador.")
    @ApiResponse(responseCode = "201", description = "Reporte registrado")
    @ApiResponse(responseCode = "400", description = "Dados invalidos")
    @ApiResponse(responseCode = "404", description = "Morador inexistente")
    public ResponseEntity<OcorrenciaResponse> reportar(@RequestBody @Valid OcorrenciaRequest req) {
        OcorrenciaResponse criada = service.reportar(req);
        return ResponseEntity.created(Uris.filho(criada.id())).body(criada);
    }

    @GetMapping
    @Operation(summary = "Listar reportes", description = "Filtros combinaveis: ?status=ABERTA&tipo=TRINCA_SOLO&setorId=2&moradorId=7")
    public PaginaResponse<OcorrenciaResponse> listar(@RequestParam(required = false) StatusOcorrencia status,
            @RequestParam(required = false) TipoOcorrencia tipo,
            @RequestParam(required = false) Long setorId,
            @RequestParam(required = false) Long moradorId,
            @ParameterObject @PageableDefault(size = 10, sort = "criadoEm", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.listar(status, tipo, setorId, moradorId, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar reporte")
    @ApiResponse(responseCode = "200", description = "Reporte encontrado")
    @ApiResponse(responseCode = "404", description = "Reporte inexistente")
    public OcorrenciaResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @GetMapping("/protocolo/{protocolo}")
    @Operation(summary = "Consultar pelo protocolo", description = "Ex.: AE-2026-DEMO02")
    @ApiResponse(responseCode = "200", description = "Reporte encontrado")
    @ApiResponse(responseCode = "404", description = "Protocolo inexistente")
    public OcorrenciaResponse buscarPorProtocolo(@PathVariable String protocolo) {
        return service.buscarPorProtocolo(protocolo);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atender reporte (mudar status)", description = "Transicoes invalidas retornam 422. DESCARTADA exige observacao.")
    @ApiResponse(responseCode = "200", description = "Status alterado")
    @ApiResponse(responseCode = "404", description = "Reporte inexistente")
    @ApiResponse(responseCode = "422", description = "Transicao invalida")
    public OcorrenciaResponse alterarStatus(@PathVariable Long id, @RequestBody @Valid StatusOcorrenciaRequest req) {
        return service.alterarStatus(id, req);
    }

    @GetMapping("/{id}/historico")
    @Operation(summary = "Historico de status do reporte")
    @ApiResponse(responseCode = "200", description = "Historico")
    @ApiResponse(responseCode = "404", description = "Reporte inexistente")
    public List<HistoricoOcorrenciaResponse> historico(@PathVariable Long id) {
        return service.historico(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir reporte", description = "Somente reportes ABERTA (ex.: enviado por engano).")
    @ApiResponse(responseCode = "204", description = "Reporte excluido")
    @ApiResponse(responseCode = "404", description = "Reporte inexistente")
    @ApiResponse(responseCode = "422", description = "Reporte ja em atendimento")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
