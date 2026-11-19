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

/** Classificacao de risco de um setor em um instante, feita pela regra de limites ou pelo modelo de ML. */
@Entity
@Table(name = "TB_AE_PREVISAO")
public class PrevisaoRisco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PREVISAO")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_SETOR", nullable = false)
    private Setor setor;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "DS_NIVEL", nullable = false, length = 10)
    private NivelRisco nivel;

    /** Probabilidade (0 a 1) informada pelo modelo; nula na regra de limites. */
    @Column(name = "NR_PROBABILIDADE", precision = 5, scale = 4)
    private BigDecimal probabilidade;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "DS_ORIGEM", nullable = false, length = 15)
    private OrigemPrevisao origem;

    @Column(name = "DS_VERSAO_MODELO", length = 30)
    private String versaoModelo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_LEITURA")
    private LeituraSensor leitura;

    @Column(name = "DT_PREVISAO", nullable = false)
    private LocalDateTime criadaEm;

    protected PrevisaoRisco() {
    }

    private PrevisaoRisco(Setor setor, NivelRisco nivel, BigDecimal probabilidade, OrigemPrevisao origem,
                          String versaoModelo, LeituraSensor leitura) {
        this.setor = setor;
        this.nivel = nivel;
        this.probabilidade = probabilidade;
        this.origem = origem;
        this.versaoModelo = versaoModelo;
        this.leitura = leitura;
        this.criadaEm = LocalDateTime.now();
    }

    /** Classifica a leitura recebida com a regra de limites do proprio setor. */
    public static PrevisaoRisco porRegraDeLimites(LeituraSensor leitura) {
        Setor setor = leitura.getEstacao().getSetor();
        return new PrevisaoRisco(setor, setor.classificar(leitura), null, OrigemPrevisao.REGRA_LIMITES,
                null, leitura);
    }

    /** Previsao publicada pelo modelo de Machine Learning (Python). */
    public static PrevisaoRisco doModelo(Setor setor, NivelRisco nivel, BigDecimal probabilidade, String versaoModelo) {
        if (probabilidade == null || probabilidade.signum() < 0 || probabilidade.compareTo(BigDecimal.ONE) > 0) {
            throw new RegraNegocioException("A probabilidade do modelo deve estar entre 0 e 1.");
        }
        return new PrevisaoRisco(setor, nivel, probabilidade, OrigemPrevisao.MODELO_ML, versaoModelo, null);
    }

    public Long getId() { return id; }
    public Setor getSetor() { return setor; }
    public NivelRisco getNivel() { return nivel; }
    public BigDecimal getProbabilidade() { return probabilidade; }
    public OrigemPrevisao getOrigem() { return origem; }
    public String getVersaoModelo() { return versaoModelo; }
    public LeituraSensor getLeitura() { return leitura; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
}
