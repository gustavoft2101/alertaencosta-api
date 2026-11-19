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

/** Escola, igreja ou equipamento publico que recebe moradores evacuados. */
@Entity
@Table(name = "TB_AE_PONTO_APOIO")
public class PontoApoio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PONTO_APOIO")
    private Long id;

    @Column(name = "NM_PONTO_APOIO", nullable = false, length = 120)
    private String nome;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "DS_TIPO", nullable = false, length = 20)
    private TipoPontoApoio tipo;

    @Column(name = "DS_ENDERECO", nullable = false, length = 200)
    private String endereco;

    @Column(name = "NR_LATITUDE", nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "NR_LONGITUDE", nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "NR_CAPACIDADE", nullable = false)
    private Integer capacidade;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "DS_SITUACAO", nullable = false, length = 10)
    private SituacaoPontoApoio situacao;

    protected PontoApoio() {
    }

    public PontoApoio(String nome, TipoPontoApoio tipo, String endereco, BigDecimal latitude,
                      BigDecimal longitude, Integer capacidade, SituacaoPontoApoio situacao) {
        atualizar(nome, tipo, endereco, latitude, longitude, capacidade, situacao);
    }

    public void atualizar(String nome, TipoPontoApoio tipo, String endereco, BigDecimal latitude,
                          BigDecimal longitude, Integer capacidade, SituacaoPontoApoio situacao) {
        this.nome = nome;
        this.tipo = tipo;
        this.endereco = endereco;
        this.latitude = latitude;
        this.longitude = longitude;
        this.capacidade = capacidade;
        this.situacao = situacao != null ? situacao : SituacaoPontoApoio.ATIVO;
    }

    public boolean isAtivo() {
        return situacao == SituacaoPontoApoio.ATIVO;
    }

    public int distanciaMetrosAte(BigDecimal lat, BigDecimal lon) {
        return Geo.distanciaMetros(lat, lon, latitude, longitude);
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public TipoPontoApoio getTipo() { return tipo; }
    public String getEndereco() { return endereco; }
    public BigDecimal getLatitude() { return latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public Integer getCapacidade() { return capacidade; }
    public SituacaoPontoApoio getSituacao() { return situacao; }
}
