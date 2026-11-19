package br.com.fiap.alertaencosta.domain.model;

import br.com.fiap.alertaencosta.domain.exception.RegraNegocioException;
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
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Estacao ESP32 instalada na encosta (pluviometro, umidade do solo e nivel da agua). */
@Entity
@Table(name = "TB_AE_ESTACAO")
public class EstacaoMonitoramento {

    /** Tolerancia para relogio do ESP32 adiantado. */
    private static final int TOLERANCIA_RELOGIO_MINUTOS = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ESTACAO")
    private Long id;

    @Column(name = "CD_ESTACAO", nullable = false, length = 30, unique = true)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_SETOR", nullable = false)
    private Setor setor;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "DS_SITUACAO", nullable = false, length = 12)
    private SituacaoEstacao situacao;

    @Column(name = "NR_LATITUDE", nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "NR_LONGITUDE", nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "DT_INSTALACAO", nullable = false, updatable = false)
    private LocalDateTime instaladaEm;

    @Column(name = "DT_ULTIMA_LEITURA")
    private LocalDateTime ultimaLeituraEm;

    protected EstacaoMonitoramento() {
    }

    public EstacaoMonitoramento(String codigo, Setor setor, BigDecimal latitude, BigDecimal longitude,
                                SituacaoEstacao situacao) {
        this.codigo = codigo.toUpperCase();
        this.instaladaEm = LocalDateTime.now();
        atualizar(setor, latitude, longitude, situacao);
    }

    public void atualizar(Setor setor, BigDecimal latitude, BigDecimal longitude, SituacaoEstacao situacao) {
        this.setor = setor;
        this.latitude = latitude;
        this.longitude = longitude;
        this.situacao = situacao != null ? situacao : SituacaoEstacao.ATIVA;
    }

    /** Fabrica da leitura: valida a estacao e o horario da medicao. */
    public LeituraSensor registrarLeitura(BigDecimal chuvaMm, BigDecimal umidadeSoloPct, BigDecimal nivelAguaCm,
                                          LocalDateTime medidaEm) {
        if (situacao != SituacaoEstacao.ATIVA) {
            throw new RegraNegocioException(
                    "A estacao %s esta %s e nao pode enviar leituras. Reative-a antes.".formatted(codigo, situacao));
        }
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime momento = medidaEm != null ? medidaEm : agora;
        if (momento.isAfter(agora.plusMinutes(TOLERANCIA_RELOGIO_MINUTOS))) {
            throw new RegraNegocioException("A data da medicao (%s) esta no futuro. Verifique o relogio da estacao %s."
                    .formatted(momento, codigo));
        }
        if (ultimaLeituraEm == null || momento.isAfter(ultimaLeituraEm)) {
            ultimaLeituraEm = momento;
        }
        return new LeituraSensor(this, chuvaMm, umidadeSoloPct, nivelAguaCm, momento);
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public Setor getSetor() { return setor; }
    public SituacaoEstacao getSituacao() { return situacao; }
    public BigDecimal getLatitude() { return latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public LocalDateTime getInstaladaEm() { return instaladaEm; }
    public LocalDateTime getUltimaLeituraEm() { return ultimaLeituraEm; }
}
