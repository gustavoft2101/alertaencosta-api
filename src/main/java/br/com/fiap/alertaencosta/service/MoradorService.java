package br.com.fiap.alertaencosta.service;

import br.com.fiap.alertaencosta.domain.exception.ConflitoException;
import br.com.fiap.alertaencosta.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.alertaencosta.domain.model.Morador;
import br.com.fiap.alertaencosta.domain.model.Setor;
import br.com.fiap.alertaencosta.repository.ConfirmacaoRepository;
import br.com.fiap.alertaencosta.repository.MoradorRepository;
import br.com.fiap.alertaencosta.repository.OcorrenciaRepository;
import br.com.fiap.alertaencosta.web.dto.MoradorAtualizacaoRequest;
import br.com.fiap.alertaencosta.web.dto.MoradorRequest;
import br.com.fiap.alertaencosta.web.dto.MoradorResponse;
import br.com.fiap.alertaencosta.web.dto.PaginaResponse;
import br.com.fiap.alertaencosta.web.dto.RotaSeguraResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MoradorService {

    private final MoradorRepository repository;
    private final OcorrenciaRepository ocorrenciaRepository;
    private final ConfirmacaoRepository confirmacaoRepository;
    private final SetorService setorService;
    private final PontoApoioService pontoApoioService;

    public MoradorService(MoradorRepository repository, OcorrenciaRepository ocorrenciaRepository,
                          ConfirmacaoRepository confirmacaoRepository, SetorService setorService,
                          PontoApoioService pontoApoioService) {
        this.repository = repository;
        this.ocorrenciaRepository = ocorrenciaRepository;
        this.confirmacaoRepository = confirmacaoRepository;
        this.setorService = setorService;
        this.pontoApoioService = pontoApoioService;
    }

    @Transactional
    public MoradorResponse cadastrar(MoradorRequest r) {
        String email = Morador.normalizarEmail(r.email());
        String cpf = Morador.somenteDigitos(r.cpf());
        if (repository.existsByEmail(email)) {
            throw new ConflitoException("Ja existe morador cadastrado com o e-mail " + email + ".");
        }
        if (repository.existsByCpf(cpf)) {
            throw new ConflitoException("Ja existe morador cadastrado com este CPF.");
        }
        Setor setor = setorService.buscarEntidade(r.setorId());
        Morador morador = new Morador(r.nome(), email, cpf, r.telefone(), r.endereco(), r.latitude(), r.longitude(),
                setor, r.necessidadeEspecial(), r.pushToken());
        return MoradorResponse.de(repository.save(morador));
    }

    @Transactional(readOnly = true)
    public PaginaResponse<MoradorResponse> listar(Long setorId, Pageable pageable) {
        var pagina = setorId == null ? repository.findAll(pageable) : repository.findBySetorId(setorId, pageable);
        return PaginaResponse.de(pagina, MoradorResponse::de);
    }

    @Transactional(readOnly = true)
    public MoradorResponse buscar(Long id) {
        return MoradorResponse.de(buscarEntidade(id));
    }

    /** Servico "Orientacao de rota segura": da casa do morador ate o ponto de apoio ativo mais proximo. */
    @Transactional(readOnly = true)
    public RotaSeguraResponse rotaSegura(Long id) {
        Morador m = buscarEntidade(id);
        return pontoApoioService.rotaAPartirDe(m.latitudeOrigem(), m.longitudeOrigem());
    }

    @Transactional
    public MoradorResponse atualizar(Long id, MoradorAtualizacaoRequest r) {
        Morador morador = buscarEntidade(id);
        String email = Morador.normalizarEmail(r.email());
        if (repository.existsByEmailAndIdNot(email, id)) {
            throw new ConflitoException("O e-mail " + email + " ja pertence a outro morador.");
        }
        Setor setor = setorService.buscarEntidade(r.setorId());
        morador.atualizar(r.nome(), email, r.telefone(), r.endereco(), r.latitude(), r.longitude(), setor,
                r.necessidadeEspecial(), r.pushToken());
        return MoradorResponse.de(morador);
    }

    @Transactional
    public void excluir(Long id) {
        Morador morador = buscarEntidade(id);
        if (ocorrenciaRepository.existsByMoradorId(id) || confirmacaoRepository.existsByMoradorId(id)) {
            throw new ConflitoException("O morador possui reportes ou respostas a alertas e nao pode ser excluido (historico da Defesa Civil).");
        }
        repository.delete(morador);
    }

    Morador buscarEntidade(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Morador", id));
    }
}
