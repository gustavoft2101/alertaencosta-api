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

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Morador cadastrado e vinculado ao setor de risco onde fica a sua casa. */
@Entity
@Table(name = "TB_AE_MORADOR")
public class Morador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_MORADOR")
    private Long id;

    @Column(name = "NM_MORADOR", nullable = false, length = 120)
    private String nome;

    @Column(name = "DS_EMAIL", nullable = false, length = 150, unique = true)
    private String email;

    @Column(name = "NR_CPF", nullable = false, length = 11, unique = true)
    private String cpf;

    @Column(name = "NR_TELEFONE", nullable = false, length = 20)
    private String telefone;

    @Column(name = "DS_ENDERECO", nullable = false, length = 200)
    private String endereco;

    @Column(name = "NR_LATITUDE", precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "NR_LONGITUDE", precision = 9, scale = 6)
    private BigDecimal longitude;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_SETOR", nullable = false)
    private Setor setor;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "DS_NECESSIDADE_ESPECIAL", nullable = false, length = 25)
    private NecessidadeEspecial necessidadeEspecial;

    /** Token do Expo Push do celular do morador (simulado no prototipo). */
    @Column(name = "DS_PUSH_TOKEN", length = 200)
    private String pushToken;

    @Column(name = "DT_CRIACAO", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    protected Morador() {
    }

    public Morador(String nome, String email, String cpf, String telefone, String endereco,
                   BigDecimal latitude, BigDecimal longitude, Setor setor,
                   NecessidadeEspecial necessidadeEspecial, String pushToken) {
        this.cpf = somenteDigitos(cpf);
        this.criadoEm = LocalDateTime.now();
        atualizar(nome, email, telefone, endereco, latitude, longitude, setor, necessidadeEspecial, pushToken);
    }

    public void atualizar(String nome, String email, String telefone, String endereco,
                          BigDecimal latitude, BigDecimal longitude, Setor setor,
                          NecessidadeEspecial necessidadeEspecial, String pushToken) {
        this.nome = nome;
        this.email = normalizarEmail(email);
        this.telefone = telefone;
        this.endereco = endereco;
        this.latitude = latitude;
        this.longitude = longitude;
        this.setor = setor;
        this.necessidadeEspecial = necessidadeEspecial != null ? necessidadeEspecial : NecessidadeEspecial.NENHUMA;
        this.pushToken = pushToken;
    }

    public boolean isPrioritario() {
        return necessidadeEspecial.isPrioritario();
    }

    public boolean podeReceberPush() {
        return pushToken != null && !pushToken.isBlank();
    }

    /** Origem da rota segura: a casa do morador, ou o centro do setor se a localizacao nao foi informada. */
    public BigDecimal latitudeOrigem() {
        return latitude != null ? latitude : setor.getLatitude();
    }

    public BigDecimal longitudeOrigem() {
        return longitude != null ? longitude : setor.getLongitude();
    }

    public static String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    public static String somenteDigitos(String valor) {
        return valor == null ? null : valor.replaceAll("\\D", "");
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getCpf() { return cpf; }
    public String getTelefone() { return telefone; }
    public String getEndereco() { return endereco; }
    public BigDecimal getLatitude() { return latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public Setor getSetor() { return setor; }
    public NecessidadeEspecial getNecessidadeEspecial() { return necessidadeEspecial; }
    public String getPushToken() { return pushToken; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}
