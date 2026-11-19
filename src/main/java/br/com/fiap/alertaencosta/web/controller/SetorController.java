package br.com.fiap.alertaencosta.web.controller;

import br.com.fiap.alertaencosta.domain.model.NivelRisco;
import br.com.fiap.alertaencosta.service.MonitoramentoService;
import br.com.fiap.alertaencosta.service.SetorService;
import br.com.fiap.alertaencosta.web.dto.PaginaResponse;
import br.com.fiap.alertaencosta.web.dto.PrevisaoRequest;
import br.com.fiap.alertaencosta.web.dto.PrevisaoResponse;
import br.com.fiap.alertaencosta.web.dto.RiscoProcessadoResponse;
import br.com.fiap.alertaencosta.web.dto.SetorAtualizacaoRequest;
import br.com.fiap.alertaencosta.web.dto.SetorRequest;
import br.com.fiap.alertaencosta.web.dto.SetorResponse;
import br.com.fiap.alertaencosta.web.dto.SetorSituacaoResponse;
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
@RequestMapping("/api/v1/setores")
@Tag(name = "Setores de risco", description = "Areas mapeadas, limites de risco, situacao em tempo real e previsoes do modelo de ML")
public class SetorController {

    private final SetorService service;
    private final MonitoramentoService monitoramento;

    public SetorController(SetorService service, MonitoramentoService monitoramento) {
        this.service = service;
        this.monitoramento = monitoramento;
    }

    @PostMapping
    @Operation(summary = "Cadastrar setor de risco", description = "Todo setor nasce com risco BAIXO.")
    @ApiResponse(responseCode = "201", description = "Setor criado")
    @ApiResponse(responseCode = "400", description = "Dados invalidos")
    @ApiResponse(responseCode = "409", description = "Codigo ja existente")
    public ResponseEntity<SetorResponse> cadastrar(@RequestBody @Valid SetorRequest req) {
        SetorResponse criado = service.cadastrar(req);
        return ResponseEntity.created(Uris.filho(criado.id())).body(criado);
    }

    @GetMapping
    @Operation(summary = "Listar setores", description = "Filtro opcional ?nivel=ALTO para o mapa da Defesa Civil.")
    public PaginaResponse<SetorResponse> listar(@RequestParam(required = false) NivelRisco nivel,
            @ParameterObject @PageableDefault(size = 20, sort = "codigo", direction = Sort.Direction.ASC) Pageable pageable) {
        return service.listar(nivel, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar setor")
    @ApiResponse(responseCode = "200", description = "Setor encontrado")
    @ApiResponse(responseCode = "404", description = "Setor inexistente")
    public SetorResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @GetMapping("/{id}/situacao")
    @Operation(summary = "Situacao do setor (painel)", description = "Nivel atual, ultima leitura, alerta ativo, moradores e prioritarios.")
    @ApiResponse(responseCode = "200", description = "Situacao")
    @ApiResponse(responseCode = "404", description = "Setor inexistente")
    public SetorSituacaoResponse situacao(@PathVariable Long id) {
        return service.situacao(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar setor", description = "Substitui nome, localizacao e limites. Codigo e nivel atual nao sao editaveis.")
    @ApiResponse(responseCode = "200", description = "Setor atualizado")
    @ApiResponse(responseCode = "404", description = "Setor inexistente")
    public SetorResponse atualizar(@PathVariable Long id, @RequestBody @Valid SetorAtualizacaoRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir setor", description = "Bloqueado (409) se houver moradores, estacoes, alertas, previsoes ou reportes.")
    @ApiResponse(responseCode = "204", description = "Setor excluido")
    @ApiResponse(responseCode = "404", description = "Setor inexistente")
    @ApiResponse(responseCode = "409", description = "Setor em uso")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/previsoes")
    @Operation(summary = "Publicar previsao do modelo de ML",
            description = "Usado pelo modelo Python. Atualiza o nivel do setor; se ALTO, emite (ou escala) o alerta automaticamente.")
    @ApiResponse(responseCode = "201", description = "Previsao registrada e processada")
    @ApiResponse(responseCode = "400", description = "Dados invalidos")
    @ApiResponse(responseCode = "404", description = "Setor inexistente")
    public ResponseEntity<RiscoProcessadoResponse> publicarPrevisao(@PathVariable Long id, @RequestBody @Valid PrevisaoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(monitoramento.registrarPrevisao(id, req));
    }

    @GetMapping("/{id}/previsoes")
    @Operation(summary = "Historico de previsoes do setor", description = "Regra de limites e modelo de ML, mais recentes primeiro.")
    @ApiResponse(responseCode = "200", description = "Previsoes")
    @ApiResponse(responseCode = "404", description = "Setor inexistente")
    public PaginaResponse<PrevisaoResponse> previsoes(@PathVariable Long id,
            @ParameterObject @PageableDefault(size = 20, sort = "criadaEm", direction = Sort.Direction.DESC) Pageable pageable) {
        return monitoramento.previsoes(id, pageable);
    }
}
