package br.com.fiap.alertaencosta.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Setor de risco mapeado (ex.: "BR-02 Margem do Corrego Bananal").
 * Guarda os limites que, quando atingidos pelas leituras, colocam o setor em risco ALTO.
 */
@Entity
@Table(name = "TB_AE_SETOR")
public class Setor {

    /** Fracao do limite a partir da qual o setor entra em atencao (MEDIO). */
    static final BigDecimal FRACAO_ATENCAO = new BigDecimal("0.70");
    /** Chuva e solo, juntos, acima desta fracao indicam solo saturado: risco ALTO mesmo sem bater 100%. */
    static final BigDecimal FRACAO_SOLO_SATURADO = new BigDecimal("0.85");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_SETOR")
    private Long id;

    @Column(name = "CD_SETOR", nullable = false, length = 10, unique = true)
    private String codigo;

    @Column(name = "NM_SETOR", nullable = false, length = 100)
    private String nome;

    @Column(name = "NM_BAIRRO", nullable = false, length = 80)
    private String bairro;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "DS_TIPO_RISCO", nullable = false, length = 15)
    private TipoRisco tipoRisco;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "DS_NIVEL_RISCO", nullable = false, length = 10)
    private NivelRisco nivelRiscoAtual;

    @Column(name = "NR_LATITUDE", nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "NR_LONGITUDE", nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "NR_LIMITE_CHUVA_MM", nullable = false, precision = 6, scale = 2)
    private BigDecimal limiteChuvaMm;

    @Column(name = "NR_LIMITE_UMIDADE_PCT", nullable = false, precision = 5, scale = 2)
    private BigDecimal limiteUmidadeSoloPct;

    /** Opcional: so setores com corrego/varzea tem sensor de nivel da agua. */
    @Column(name = "NR_LIMITE_NIVEL_AGUA_CM", precision = 6, scale = 2)
    private BigDecimal limiteNivelAguaCm;

    @Column(name = "DT_ATUALIZACAO_RISCO", nullable = false)
    private LocalDateTime riscoAtualizadoEm;

    protected Setor() {
    }

    public Setor(String codigo, String nome, String bairro, TipoRisco tipoRisco,
                 BigDecimal latitude, BigDecimal longitude,
                 BigDecimal limiteChuvaMm, BigDecimal limiteUmidadeSoloPct, BigDecimal limiteNivelAguaCm) {
        this.codigo = codigo.toUpperCase();
        this.nivelRiscoAtual = NivelRisco.BAIXO;
        this.riscoAtualizadoEm = LocalDateTime.now();
        atualizar(nome, bairro, tipoRisco, latitude, longitude, limiteChuvaMm, limiteUmidadeSoloPct, limiteNivelAguaCm);
    }

    public void atualizar(String nome, String bairro, TipoRisco tipoRisco,
                          BigDecimal latitude, BigDecimal longitude,
                          BigDecimal limiteChuvaMm, BigDecimal limiteUmidadeSoloPct, BigDecimal limiteNivelAguaCm) {
        this.nome = nome;
        this.bairro = bairro;
        this.tipoRisco = tipoRisco;
        this.latitude = latitude;
        this.longitude = longitude;
        this.limiteChuvaMm = limiteChuvaMm;
        this.limiteUmidadeSoloPct = limiteUmidadeSoloPct;
        this.limiteNivelAguaCm = limiteNivelAguaCm;
    }

    /**
     * Regra de limites (fallback local quando o modelo de ML nao responde):
     * <ul>
     *   <li>ALTO: qualquer indicador atingiu 100% do limite do setor, ou chuva e umidade do solo
     *       estao ambas acima de 85% (solo saturado + chuva = deslizamento iminente);</li>
     *   <li>MEDIO: algum indicador passou de 70% do limite;</li>
     *   <li>BAIXO: caso contrario.</li>
     * </ul>
     */
    public NivelRisco classificar(LeituraSensor leitura) {
        BigDecimal chuva = fracao(leitura.getChuvaMm(), limiteChuvaMm);
        BigDecimal solo = fracao(leitura.getUmidadeSoloPct(), limiteUmidadeSoloPct);
        BigDecimal agua = fracao(leitura.getNivelAguaCm(), limiteNivelAguaCm);
        BigDecimal maior = chuva.max(solo).max(agua);

        if (maior.compareTo(BigDecimal.ONE) >= 0
                || (chuva.compareTo(FRACAO_SOLO_SATURADO) >= 0 && solo.compareTo(FRACAO_SOLO_SATURADO) >= 0)) {
            return NivelRisco.ALTO;
        }
        return maior.compareTo(FRACAO_ATENCAO) >= 0 ? NivelRisco.MEDIO : NivelRisco.BAIXO;
    }

    /** @return o nivel anterior */
    public NivelRisco atualizarRisco(NivelRisco novoNivel) {
        NivelRisco anterior = this.nivelRiscoAtual;
        this.nivelRiscoAtual = novoNivel;
        this.riscoAtualizadoEm = LocalDateTime.now();
        return anterior;
    }

    private static BigDecimal fracao(BigDecimal valor, BigDecimal limite) {
        if (valor == null || limite == null || limite.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return valor.divide(limite, 4, RoundingMode.HALF_UP);
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNome() { return nome; }
    public String getBairro() { return bairro; }
    public TipoRisco getTipoRisco() { return tipoRisco; }
    public NivelRisco getNivelRiscoAtual() { return nivelRiscoAtual; }
    public BigDecimal getLatitude() { return latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public BigDecimal getLimiteChuvaMm() { return limiteChuvaMm; }
    public BigDecimal getLimiteUmidadeSoloPct() { return limiteUmidadeSoloPct; }
    public BigDecimal getLimiteNivelAguaCm() { return limiteNivelAguaCm; }
    public LocalDateTime getRiscoAtualizadoEm() { return riscoAtualizadoEm; }
}
