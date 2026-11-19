package br.com.fiap.alertaencosta.service;

import br.com.fiap.alertaencosta.domain.exception.ConflitoException;
import br.com.fiap.alertaencosta.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.alertaencosta.domain.model.PontoApoio;
import br.com.fiap.alertaencosta.domain.model.SituacaoPontoApoio;
import br.com.fiap.alertaencosta.repository.AlertaRepository;
import br.com.fiap.alertaencosta.repository.PontoApoioRepository;
import br.com.fiap.alertaencosta.web.dto.PaginaResponse;
import br.com.fiap.alertaencosta.web.dto.PontoApoioRequest;
import br.com.fiap.alertaencosta.web.dto.PontoApoioResponse;
import br.com.fiap.alertaencosta.web.dto.RotaSeguraResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Optional;

@Service
public class PontoApoioService {

    private final PontoApoioRepository repository;
    private final AlertaRepository alertaRepository;

    public PontoApoioService(PontoApoioRepository repository, AlertaRepository alertaRepository) {
        this.repository = repository;
        this.alertaRepository = alertaRepository;
    }

    @Transactional
    public PontoApoioResponse cadastrar(PontoApoioRequest r) {
        return PontoApoioResponse.de(repository.save(new PontoApoio(r.nome(), r.tipo(), r.endereco(),
                r.latitude(), r.longitude(), r.capacidade(), r.situacao())));
    }

    @Transactional(readOnly = true)
    public PaginaResponse<PontoApoioResponse> listar(SituacaoPontoApoio situacao, Pageable pageable) {
        var pagina = situacao == null ? repository.findAll(pageable) : repository.findBySituacao(situacao, pageable);
        return PaginaResponse.de(pagina, PontoApoioResponse::de);
    }

    @Transactional(readOnly = true)
    public PontoApoioResponse buscar(Long id) {
        return PontoApoioResponse.de(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public RotaSeguraResponse rotaAPartirDe(BigDecimal latitude, BigDecimal longitude) {
        PontoApoio ponto = maisProximo(latitude, longitude)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ponto de apoio ativo", "qualquer"));
        return RotaSeguraResponse.de(latitude, longitude, ponto);
    }

    @Transactional
    public PontoApoioResponse atualizar(Long id, PontoApoioRequest r) {
        PontoApoio ponto = buscarEntidade(id);
        ponto.atualizar(r.nome(), r.tipo(), r.endereco(), r.latitude(), r.longitude(), r.capacidade(), r.situacao());
        return PontoApoioResponse.de(ponto);
    }

    @Transactional
    public void excluir(Long id) {
        PontoApoio ponto = buscarEntidade(id);
        if (alertaRepository.existsByPontoApoioId(id)) {
            throw new ConflitoException("O ponto de apoio ja foi indicado em alertas e nao pode ser excluido. Use situacao INATIVO.");
        }
        repository.delete(ponto);
    }

    /** Ponto ATIVO mais proximo em linha reta. Pontos INATIVO nunca entram na rota. */
    Optional<PontoApoio> maisProximo(BigDecimal latitude, BigDecimal longitude) {
        return repository.findBySituacao(SituacaoPontoApoio.ATIVO).stream()
                .min(Comparator.comparingInt(p -> p.distanciaMetrosAte(latitude, longitude)));
    }

    PontoApoio buscarEntidade(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Ponto de apoio", id));
    }
}
