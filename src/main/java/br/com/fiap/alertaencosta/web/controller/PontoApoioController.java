package br.com.fiap.alertaencosta.web.controller;

import br.com.fiap.alertaencosta.domain.model.SituacaoPontoApoio;
import br.com.fiap.alertaencosta.service.PontoApoioService;
import br.com.fiap.alertaencosta.web.dto.PaginaResponse;
import br.com.fiap.alertaencosta.web.dto.PontoApoioRequest;
import br.com.fiap.alertaencosta.web.dto.PontoApoioResponse;
import br.com.fiap.alertaencosta.web.dto.RotaSeguraResponse;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
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
@Validated
@RequestMapping("/api/v1/pontos-apoio")
@Tag(name = "Pontos de apoio", description = "Escolas, igrejas e equipamentos que recebem moradores evacuados")
public class PontoApoioController {

    private final PontoApoioService service;

    public PontoApoioController(PontoApoioService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Cadastrar ponto de apoio")
    @ApiResponse(responseCode = "201", description = "Ponto criado")
    @ApiResponse(responseCode = "400", description = "Dados invalidos")
    public ResponseEntity<PontoApoioResponse> cadastrar(@RequestBody @Valid PontoApoioRequest req) {
        PontoApoioResponse criado = service.cadastrar(req);
        return ResponseEntity.created(Uris.filho(criado.id())).body(criado);
    }

    @GetMapping
    @Operation(summary = "Listar pontos de apoio", description = "Filtro opcional ?situacao=ATIVO.")
    public PaginaResponse<PontoApoioResponse> listar(@RequestParam(required = false) SituacaoPontoApoio situacao,
            @ParameterObject @PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        return service.listar(situacao, pageable);
    }

    @GetMapping("/mais-proximo")
    @Operation(summary = "Rota segura a partir de uma coordenada",
            description = "Ponto de apoio ATIVO mais proximo, distancia, tempo a pe e link do mapa. Usado pelo app com o GPS do celular.")
    @ApiResponse(responseCode = "200", description = "Rota calculada")
    @ApiResponse(responseCode = "400", description = "Coordenada invalida ou ausente")
    @ApiResponse(responseCode = "404", description = "Nenhum ponto de apoio ativo")
    public RotaSeguraResponse maisProximo(
            @RequestParam @DecimalMin("-90") @DecimalMax("90") BigDecimal latitude,
            @RequestParam @DecimalMin("-180") @DecimalMax("180") BigDecimal longitude) {
        return service.rotaAPartirDe(latitude, longitude);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar ponto de apoio")
    @ApiResponse(responseCode = "200", description = "Ponto encontrado")
    @ApiResponse(responseCode = "404", description = "Ponto inexistente")
    public PontoApoioResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar ponto de apoio", description = "Use situacao INATIVO para tira-lo das rotas (ex.: lotado, interditado).")
    @ApiResponse(responseCode = "200", description = "Ponto atualizado")
    @ApiResponse(responseCode = "404", description = "Ponto inexistente")
    public PontoApoioResponse atualizar(@PathVariable Long id, @RequestBody @Valid PontoApoioRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir ponto de apoio", description = "Bloqueado (409) se ja foi indicado em algum alerta.")
    @ApiResponse(responseCode = "204", description = "Ponto excluido")
    @ApiResponse(responseCode = "404", description = "Ponto inexistente")
    @ApiResponse(responseCode = "409", description = "Ponto usado em alertas")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
