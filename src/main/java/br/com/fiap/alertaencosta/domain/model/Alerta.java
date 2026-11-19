package br.com.fiap.alertaencosta.domain.model;

import br.com.fiap.alertaencosta.domain.exception.RegraNegocioException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Alerta de risco para todos os moradores de um setor, com a rota ate o ponto de apoio.
 * E o agregado que concentra as regras de emissao, escalonamento, confirmacao e encerramento.
 */
@Entity
@Table(name = "TB_AE_ALERTA")
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ALERTA")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_SETOR", nullable = false)
    private Setor setor;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "DS_NIVEL", nullable = false, length = 10)
    private NivelRisco nivel;

    @Column(name = "DS_MENSAGEM", nullable = false, length = 500)
    private String mensagem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PONTO_APOIO")
    private PontoApoio pontoApoio;

    @Column(name = "NR_DISTANCIA_METROS")
    private Integer distanciaMetros;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "DS_ORIGEM", nullable = false, length = 12)
    private OrigemAlerta origem;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "DS_STATUS", nullable = false, length = 10)
    private StatusAlerta status;

    @Column(name = "QT_NOTIFICADOS", nullable = false)
    private Integer totalNotificados;

    @Column(name = "DT_EMISSAO", nullable = false, updatable = false)
    private LocalDateTime emitidoEm;

    @Column(name = "DT_ENCERRAMENTO")
    private LocalDateTime encerradoEm;

    @Column(name = "DS_MOTIVO_ENCERRAMENTO", length = 300)
    private String motivoEncerramento;

    @OneToMany(mappedBy = "alerta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConfirmacaoAlerta> confirmacoes = new ArrayList<>();

    protected Alerta() {
    }

    /**
     * Emite um alerta. Nivel BAIXO nao gera alerta. Sem ponto de apoio ativo, a mensagem orienta
     * a procurar local alto e acionar a Defesa Civil (199).
     */
    public static Alerta emitir(Setor setor, NivelRisco nivel, PontoApoio pontoApoio, OrigemAlerta origem,
                                String mensagemPersonalizada) {
        if (nivel == null || nivel == NivelRisco.BAIXO) {
            throw new RegraNegocioException("Alertas so podem ser emitidos para risco MEDIO ou ALTO.");
        }
        if (pontoApoio != null && !pontoApoio.isAtivo()) {
            throw new RegraNegocioException("O ponto de apoio '%s' esta INATIVO e nao pode ser indicado."
                    .formatted(pontoApoio.getNome()));
        }
        Alerta a = new Alerta();
        a.setor = Objects.requireNonNull(setor);
        a.nivel = nivel;
        a.pontoApoio = pontoApoio;
        a.distanciaMetros = pontoApoio == null ? null
                : pontoApoio.distanciaMetrosAte(setor.getLatitude(), setor.getLongitude());
        a.origem = origem;
        a.status = StatusAlerta.ATIVO;
        a.totalNotificados = 0;
        a.emitidoEm = LocalDateTime.now();
        a.mensagem = (mensagemPersonalizada != null && !mensagemPersonalizada.isBlank())
                ? mensagemPersonalizada.trim() : a.montarMensagem();
        return a;
    }

    /** Eleva o nivel de um alerta ativo (ex.: MEDIO -> ALTO). @return true se mudou */
    public boolean escalar(NivelRisco novoNivel) {
        exigirAtivo();
        if (!novoNivel.isMaiorQue(nivel)) {
            return false;
        }
        this.nivel = novoNivel;
        this.mensagem = montarMensagem();
        return true;
    }

    public void encerrar(String motivo) {
        exigirAtivo();
        if (motivo == null || motivo.isBlank()) {
            throw new RegraNegocioException("Informe o motivo do encerramento do alerta.");
        }
        this.status = StatusAlerta.ENCERRADO;
        this.encerradoEm = LocalDateTime.now();
        this.motivoEncerramento = motivo.trim();
    }

    /**
     * Registra (ou atualiza) a resposta do morador. Idempotente: o mesmo morador sempre tem uma
     * unica confirmacao por alerta, e pode mudar de PRECISA_AJUDA para SEGURO depois do resgate.
     */
    public ConfirmacaoAlerta confirmar(Morador morador, SituacaoMorador situacao, String observacao) {
        exigirAtivo();
        if (!morador.getSetor().getId().equals(setor.getId())) {
            throw new RegraNegocioException("O morador %d pertence ao setor %s, e este alerta e do setor %s."
                    .formatted(morador.getId(), morador.getSetor().getCodigo(), setor.getCodigo()));
        }
        Optional<ConfirmacaoAlerta> existente = confirmacaoDe(morador.getId());
        if (existente.isPresent()) {
            existente.get().atualizar(situacao, observacao);
            return existente.get();
        }
        ConfirmacaoAlerta nova = new ConfirmacaoAlerta(this, morador, situacao, observacao);
        confirmacoes.add(nova);
        return nova;
    }

    public Optional<ConfirmacaoAlerta> confirmacaoDe(Long moradorId) {
        return confirmacoes.stream().filter(c -> c.getMorador().getId().equals(moradorId)).findFirst();
    }

    public void registrarNotificacao(int quantidade) {
        this.totalNotificados = quantidade;
    }

    public boolean isAtivo() {
        return status == StatusAlerta.ATIVO;
    }

    public long contar(SituacaoMorador situacao) {
        return confirmacoes.stream().filter(c -> c.getSituacao() == situacao).count();
    }

    private void exigirAtivo() {
        if (!isAtivo()) {
            throw new RegraNegocioException("O alerta %d ja foi ENCERRADO.".formatted(id));
        }
    }

    private String montarMensagem() {
        String risco = switch (setor.getTipoRisco()) {
            case DESLIZAMENTO -> "deslizamento";
            case ALAGAMENTO -> "alagamento";
            case MISTO -> "deslizamento e alagamento";
        };
        String local = "setor %s (%s)".formatted(setor.getCodigo(), setor.getNome());
        String rota = pontoApoio == null
                ? "Nenhum ponto de apoio disponivel: procure um local alto e seguro."
                : "Va a pe ate %s - %s (%d m, cerca de %d min)."
                .formatted(pontoApoio.getNome(), pontoApoio.getEndereco(), distanciaMetros, Geo.minutosAPe(distanciaMetros));
        if (nivel == NivelRisco.ALTO) {
            return "ALERTA ALTO de %s no %s. Saia de casa agora. %s Emergencia: Defesa Civil 199."
                    .formatted(risco, local, rota);
        }
        return "ATENCAO: risco MEDIO de %s no %s. Separe documentos e remedios e fique atento ao app. %s"
                .formatted(risco, local, rota);
    }

    public Long getId() { return id; }
    public Setor getSetor() { return setor; }
    public NivelRisco getNivel() { return nivel; }
    public String getMensagem() { return mensagem; }
    public PontoApoio getPontoApoio() { return pontoApoio; }
    public Integer getDistanciaMetros() { return distanciaMetros; }
    public OrigemAlerta getOrigem() { return origem; }
    public StatusAlerta getStatus() { return status; }
    public Integer getTotalNotificados() { return totalNotificados; }
    public LocalDateTime getEmitidoEm() { return emitidoEm; }
    public LocalDateTime getEncerradoEm() { return encerradoEm; }
    public String getMotivoEncerramento() { return motivoEncerramento; }
    public List<ConfirmacaoAlerta> getConfirmacoes() { return Collections.unmodifiableList(confirmacoes); }
}
