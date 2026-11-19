package br.com.fiap.alertaencosta.web.controller;

import br.com.fiap.alertaencosta.service.MoradorService;
import br.com.fiap.alertaencosta.web.dto.MoradorAtualizacaoRequest;
import br.com.fiap.alertaencosta.web.dto.MoradorRequest;
import br.com.fiap.alertaencosta.web.dto.MoradorResponse;
import br.com.fiap.alertaencosta.web.dto.PaginaResponse;
import br.com.fiap.alertaencosta.web.dto.RotaSeguraResponse;
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
@RequestMapping("/api/v1/moradores")
@Tag(name = "Moradores", description = "Cadastro do morador e do setor de risco onde mora")
public class MoradorController {

    private final MoradorService service;

    public MoradorController(MoradorService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Cadastrar morador", description = "E-mail e CPF unicos. Necessidade especial diferente de NENHUMA = prioridade na busca ativa.")
    @ApiResponse(responseCode = "201", description = "Morador criado")
    @ApiResponse(responseCode = "400", description = "Dados invalidos")
    @ApiResponse(responseCode = "404", description = "Setor inexistente")
    @ApiResponse(responseCode = "409", description = "E-mail ou CPF ja cadastrado")
    public ResponseEntity<MoradorResponse> cadastrar(@RequestBody @Valid MoradorRequest req) {
        MoradorResponse criado = service.cadastrar(req);
        return ResponseEntity.created(Uris.filho(criado.id())).body(criado);
    }

    @GetMapping
    @Operation(summary = "Listar moradores", description = "Filtro opcional ?setorId=2.")
    public PaginaResponse<MoradorResponse> listar(@RequestParam(required = false) Long setorId,
            @ParameterObject @PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        return service.listar(setorId, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar morador")
    @ApiResponse(responseCode = "200", description = "Morador encontrado")
    @ApiResponse(responseCode = "404", description = "Morador inexistente")
    public MoradorResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @GetMapping("/{id}/rota-segura")
    @Operation(summary = "Rota segura do morador", description = "Da casa do morador (ou do centro do setor) ate o ponto de apoio ativo mais proximo.")
    @ApiResponse(responseCode = "200", description = "Rota calculada")
    @ApiResponse(responseCode = "404", description = "Morador inexistente ou nenhum ponto ativo")
    public RotaSeguraResponse rotaSegura(@PathVariable Long id) {
        return service.rotaSegura(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar morador", description = "Substitui o cadastro. CPF imutavel.")
    @ApiResponse(responseCode = "200", description = "Morador atualizado")
    @ApiResponse(responseCode = "404", description = "Morador ou setor inexistente")
    @ApiResponse(responseCode = "409", description = "E-mail pertence a outro morador")
    public MoradorResponse atualizar(@PathVariable Long id, @RequestBody @Valid MoradorAtualizacaoRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir morador", description = "Bloqueado (409) se tiver reportes ou respostas a alertas.")
    @ApiResponse(responseCode = "204", description = "Morador excluido")
    @ApiResponse(responseCode = "404", description = "Morador inexistente")
    @ApiResponse(responseCode = "409", description = "Morador com historico")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
