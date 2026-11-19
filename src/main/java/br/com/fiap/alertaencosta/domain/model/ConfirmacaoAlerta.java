package br.com.fiap.alertaencosta.domain.model;

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
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/** Resposta do morador ao alerta: "estou seguro" ou "preciso de ajuda". Uma por morador e alerta. */
@Entity
@Table(name = "TB_AE_CONFIRMACAO",
        uniqueConstraints = @UniqueConstraint(name = "UK_AE_CONF_ALERTA_MORADOR",
                columnNames = {"ID_ALERTA", "ID_MORADOR"}))
public class ConfirmacaoAlerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CONFIRMACAO")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_ALERTA", nullable = false)
    private Alerta alerta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_MORADOR", nullable = false)
    private Morador morador;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "DS_SITUACAO", nullable = false, length = 15)
    private SituacaoMorador situacao;

    @Column(name = "DS_OBSERVACAO", length = 300)
    private String observacao;

    @Column(name = "DT_CONFIRMACAO", nullable = false)
    private LocalDateTime registradaEm;

    protected ConfirmacaoAlerta() {
    }

    ConfirmacaoAlerta(Alerta alerta, Morador morador, SituacaoMorador situacao, String observacao) {
        this.alerta = alerta;
        this.morador = morador;
        atualizar(situacao, observacao);
    }

    void atualizar(SituacaoMorador situacao, String observacao) {
        this.situacao = situacao;
        this.observacao = observacao;
        this.registradaEm = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Alerta getAlerta() { return alerta; }
    public Morador getMorador() { return morador; }
    public SituacaoMorador getSituacao() { return situacao; }
    public String getObservacao() { return observacao; }
    public LocalDateTime getRegistradaEm() { return registradaEm; }
}
