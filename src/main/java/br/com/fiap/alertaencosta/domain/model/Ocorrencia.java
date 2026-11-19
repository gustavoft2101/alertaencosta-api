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
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Reporte feito pelo morador (trinca, agua subindo...) com foto e localizacao,
 * atendido pela Defesa Civil por um fluxo de status auditavel.
 */
@Entity
@Table(name = "TB_AE_OCORRENCIA")
public class Ocorrencia {

    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_OCORRENCIA")
    private Long id;

    @Column(name = "CD_PROTOCOLO", nullable = false, length = 20, unique = true, updatable = false)
    private String protocolo;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "DS_TIPO", nullable = false, length = 20)
    private TipoOcorrencia tipo;

    @Column(name = "DS_DESCRICAO", nullable = false, length = 1000)
    private String descricao;

    @Column(name = "DS_FOTO_URL", length = 500)
    private String fotoUrl;

    @Column(name = "NR_LATITUDE", nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "NR_LONGITUDE", nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "DS_STATUS", nullable = false, length = 15)
    private StatusOcorrencia status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_MORADOR", nullable = false)
    private Morador morador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_SETOR", nullable = false)
    private Setor setor;

    @Column(name = "DT_CRIACAO", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "DT_ATUALIZACAO", nullable = false)
    private LocalDateTime atualizadoEm;

    @Column(name = "DT_RESOLUCAO")
    private LocalDateTime resolvidoEm;

    @OneToMany(mappedBy = "ocorrencia", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("alteradoEm ASC, id ASC")
    private List<HistoricoOcorrencia> historico = new ArrayList<>();

    protected Ocorrencia() {
    }

    /** O reporte fica vinculado ao setor do morador que o enviou. */
    public static Ocorrencia reportar(TipoOcorrencia tipo, String descricao, String fotoUrl,
                                      BigDecimal latitude, BigDecimal longitude, Morador morador) {
        Ocorrencia o = new Ocorrencia();
        o.protocolo = "AE-" + Year.now().getValue() + "-" + sufixoAleatorio();
        o.tipo = tipo;
        o.descricao = descricao;
        o.fotoUrl = fotoUrl;
        o.latitude = latitude;
        o.longitude = longitude;
        o.morador = morador;
        o.setor = morador.getSetor();
        o.status = StatusOcorrencia.ABERTA;
        o.criadoEm = LocalDateTime.now();
        o.atualizadoEm = o.criadoEm;
        o.historico.add(new HistoricoOcorrencia(o, null, StatusOcorrencia.ABERTA, "Reporte enviado pelo morador."));
        return o;
    }

    public void alterarStatus(StatusOcorrencia novo, String observacao) {
        if (!status.podeTransicionarPara(novo)) {
            throw new RegraNegocioException(
                    "Transicao de status invalida: %s -> %s. Permitidas a partir de %s: %s."
                            .formatted(status, novo, status, status.proximosPermitidos()));
        }
        if (novo == StatusOcorrencia.DESCARTADA && (observacao == null || observacao.isBlank())) {
            throw new RegraNegocioException("Informe uma observacao justificando o descarte do reporte.");
        }
        StatusOcorrencia anterior = status;
        status = novo;
        atualizadoEm = LocalDateTime.now();
        if (novo == StatusOcorrencia.RESOLVIDA) {
            resolvidoEm = atualizadoEm;
        }
        historico.add(new HistoricoOcorrencia(this, anterior, novo, observacao));
    }

    public void validarExclusao() {
        if (status != StatusOcorrencia.ABERTA) {
            throw new RegraNegocioException(
                    "Somente reportes ABERTA podem ser excluidos. O reporte %s esta %s e faz parte do historico da Defesa Civil."
                            .formatted(protocolo, status));
        }
    }

    private static String sufixoAleatorio() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(ALFABETO.charAt(RANDOM.nextInt(ALFABETO.length())));
        }
        return sb.toString();
    }

    public Long getId() { return id; }
    public String getProtocolo() { return protocolo; }
    public TipoOcorrencia getTipo() { return tipo; }
    public String getDescricao() { return descricao; }
    public String getFotoUrl() { return fotoUrl; }
    public BigDecimal getLatitude() { return latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public StatusOcorrencia getStatus() { return status; }
    public Morador getMorador() { return morador; }
    public Setor getSetor() { return setor; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public LocalDateTime getResolvidoEm() { return resolvidoEm; }
    public List<HistoricoOcorrencia> getHistorico() { return Collections.unmodifiableList(historico); }
}
