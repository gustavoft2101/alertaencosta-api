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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/** Trilha de auditoria de cada mudanca de status de uma ocorrencia. */
@Entity
@Table(name = "TB_AE_HISTORICO_OCORRENCIA")
public class HistoricoOcorrencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_HISTORICO")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_OCORRENCIA", nullable = false)
    private Ocorrencia ocorrencia;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "DS_STATUS_ANTERIOR", length = 15)
    private StatusOcorrencia statusAnterior;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "DS_STATUS_NOVO", nullable = false, length = 15)
    private StatusOcorrencia statusNovo;

    @Column(name = "DS_OBSERVACAO", length = 500)
    private String observacao;

    @Column(name = "DT_ALTERACAO", nullable = false)
    private LocalDateTime alteradoEm;

    protected HistoricoOcorrencia() {
    }

    HistoricoOcorrencia(Ocorrencia ocorrencia, StatusOcorrencia statusAnterior, StatusOcorrencia statusNovo,
                        String observacao) {
        this.ocorrencia = ocorrencia;
        this.statusAnterior = statusAnterior;
        this.statusNovo = statusNovo;
        this.observacao = observacao;
        this.alteradoEm = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Ocorrencia getOcorrencia() { return ocorrencia; }
    public StatusOcorrencia getStatusAnterior() { return statusAnterior; }
    public StatusOcorrencia getStatusNovo() { return statusNovo; }
    public String getObservacao() { return observacao; }
    public LocalDateTime getAlteradoEm() { return alteradoEm; }
}
