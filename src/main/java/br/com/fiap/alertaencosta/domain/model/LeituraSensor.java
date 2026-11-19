package br.com.fiap.alertaencosta.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Medicao enviada pela estacao: chuva acumulada (mm), umidade do solo (%) e nivel da agua (cm). */
@Entity
@Table(name = "TB_AE_LEITURA")
public class LeituraSensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_LEITURA")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_ESTACAO", nullable = false)
    private EstacaoMonitoramento estacao;

    @Column(name = "NR_CHUVA_MM", nullable = false, precision = 6, scale = 2)
    private BigDecimal chuvaMm;

    @Column(name = "NR_UMIDADE_SOLO_PCT", nullable = false, precision = 5, scale = 2)
    private BigDecimal umidadeSoloPct;

    @Column(name = "NR_NIVEL_AGUA_CM", precision = 6, scale = 2)
    private BigDecimal nivelAguaCm;

    @Column(name = "DT_MEDICAO", nullable = false)
    private LocalDateTime medidaEm;

    @Column(name = "DT_RECEBIMENTO", nullable = false)
    private LocalDateTime recebidaEm;

    protected LeituraSensor() {
    }

    LeituraSensor(EstacaoMonitoramento estacao, BigDecimal chuvaMm, BigDecimal umidadeSoloPct,
                  BigDecimal nivelAguaCm, LocalDateTime medidaEm) {
        this.estacao = estacao;
        this.chuvaMm = chuvaMm;
        this.umidadeSoloPct = umidadeSoloPct;
        this.nivelAguaCm = nivelAguaCm;
        this.medidaEm = medidaEm;
        this.recebidaEm = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public EstacaoMonitoramento getEstacao() { return estacao; }
    public BigDecimal getChuvaMm() { return chuvaMm; }
    public BigDecimal getUmidadeSoloPct() { return umidadeSoloPct; }
    public BigDecimal getNivelAguaCm() { return nivelAguaCm; }
    public LocalDateTime getMedidaEm() { return medidaEm; }
    public LocalDateTime getRecebidaEm() { return recebidaEm; }
}
