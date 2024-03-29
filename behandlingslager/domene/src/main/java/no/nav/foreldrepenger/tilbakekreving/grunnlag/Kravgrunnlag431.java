package no.nav.foreldrepenger.tilbakekreving.grunnlag;

import static no.nav.vedtak.log.util.LoggerUtils.mask;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagOmrådeKode;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.FagsystemId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;

@Entity(name = "Kravgrunnlag431")
@Table(name = "KRAV_GRUNNLAG_431")
public class Kravgrunnlag431 extends BaseEntitet {

    public static final DateTimeFormatter KONTROLL_FELT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS");

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_KRAV_GRUNNLAG_431")
    private Long id;

    @Column(name = "ekstern_kravgrunnlag_id", nullable = false, updatable = false)
    private String eksternKravgrunnlagId;

    @Column(name = "vedtak_id", nullable = false, updatable = false)
    private Long vedtakId;

    @Convert(converter = KravStatusKode.KodeverdiConverter.class)
    @Column(name = "krav_status_kode", nullable = false, updatable = false)
    private KravStatusKode kravStatusKode;

    @Convert(converter = FagOmrådeKode.KodeverdiConverter.class)
    @Column(name = "fag_omraade_kode", nullable = false)
    private FagOmrådeKode fagOmrådeKode;

    @Column(name = "fagsystem_id", nullable = false, updatable = false)
    private String fagSystemId;

    @Column(name = "vedtak_fagsystem_dato")
    private LocalDate vedtakFagSystemDato;

    @Column(name = "omgjort_vedtak_id")
    private Long omgjortVedtakId;

    @Column(name = "gjelder_vedtak_id", nullable = false, updatable = false)
    private String gjelderVedtakId;

    @Convert(converter = GjelderType.KodeverdiConverter.class)
    @Column(name = "gjelder_type", nullable = false, updatable = false)
    private GjelderType gjelderType;

    @Column(name = "utbetales_til_id", nullable = false, updatable = false)
    private String utbetalesTilId;

    @Convert(converter = GjelderType.KodeverdiConverter.class)
    @Column(name = "utbet_id_type", nullable = false, updatable = false)
    private GjelderType utbetGjelderType;

    @Column(name = "hjemmel_kode")
    private String hjemmelKode;

    @Column(name = "beregnes_renter")
    private String beregnesRenter;

    @Column(name = "ansvarlig_enhet", nullable = false)
    private String ansvarligEnhet;

    @Column(name = "bosted_enhet", nullable = false, updatable = false)
    private String bostedEnhet;

    @Column(name = "behandl_enhet", nullable = false, updatable = false)
    private String behandlendeEnhet;

    @Column(name = "kontroll_felt", nullable = false, updatable = false)
    private String kontrollFelt;

    @Column(name = "saksbeh_id", nullable = false)
    private String saksBehId;

    @AttributeOverrides({
            @AttributeOverride(name = "henvisning", column = @Column(name = "referanse"))
    })
    private Henvisning referanse;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "kravgrunnlag431")
    private List<KravgrunnlagPeriode432> perioder = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVedtakId() {
        return vedtakId;
    }

    public String getEksternKravgrunnlagId() {
        return eksternKravgrunnlagId;
    }

    public KravStatusKode getKravStatusKode() {
        return kravStatusKode;
    }

    public FagOmrådeKode getFagOmrådeKode() {
        return fagOmrådeKode;
    }

    public Saksnummer getSaksnummer() {
        return FagsystemId.parse(fagSystemId).getSaksnummer();
    }

    public LocalDate getVedtakFagSystemDato() {
        return vedtakFagSystemDato;
    }

    public Long getOmgjortVedtakId() {
        return omgjortVedtakId;
    }

    public String getGjelderVedtakId() {
        return gjelderVedtakId;
    }

    public GjelderType getGjelderType() {
        return gjelderType;
    }

    public String getUtbetalesTilId() {
        return utbetalesTilId;
    }

    public GjelderType getUtbetGjelderType() {
        return utbetGjelderType;
    }

    public String getHjemmelKode() {
        return hjemmelKode;
    }

    public String getBeregnesRenter() {
        return beregnesRenter;
    }

    public String getAnsvarligEnhet() {
        return ansvarligEnhet;
    }

    public String getBostedEnhet() {
        return bostedEnhet;
    }

    public String getBehandlendeEnhet() {
        return behandlendeEnhet;
    }

    public String getKontrollFelt() {
        return kontrollFelt;
    }

    public LocalDateTime getKontrollFeltAsLocalDateTime() {
        return LocalDateTime.parse(kontrollFelt, KONTROLL_FELT_FORMAT);
    }

    public LocalDate getKontrollFeltAsLocalDate() {
        return LocalDateTime.parse(kontrollFelt, KONTROLL_FELT_FORMAT).toLocalDate();
    }

    public String getSaksBehId() {
        return saksBehId;
    }

    public Henvisning getReferanse() {
        return referanse;
    }

    public List<KravgrunnlagPeriode432> getPerioder() {
        return Collections.unmodifiableList(perioder);
    }

    public void leggTilPeriode(KravgrunnlagPeriode432 periode432) {
        Objects.requireNonNull(periode432, "periode432");
        perioder.add(periode432);
    }

    public boolean gjelderEngangsstønad() {
        return FagOmrådeKode.ENGANGSSTØNAD.equals(this.fagOmrådeKode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Kravgrunnlag431 that = (Kravgrunnlag431) o;
        return Objects.equals(eksternKravgrunnlagId, that.eksternKravgrunnlagId) &&
            Objects.equals(vedtakId, that.vedtakId) &&
            kravStatusKode == that.kravStatusKode &&
            fagOmrådeKode == that.fagOmrådeKode &&
            Objects.equals(fagSystemId, that.fagSystemId) &&
            Objects.equals(vedtakFagSystemDato, that.vedtakFagSystemDato) &&
            Objects.equals(omgjortVedtakId, that.omgjortVedtakId) &&
            Objects.equals(gjelderVedtakId, that.gjelderVedtakId) &&
            gjelderType == that.gjelderType &&
            Objects.equals(utbetalesTilId, that.utbetalesTilId) &&
            utbetGjelderType == that.utbetGjelderType &&
            Objects.equals(hjemmelKode, that.hjemmelKode) &&
            Objects.equals(beregnesRenter, that.beregnesRenter) &&
            Objects.equals(ansvarligEnhet, that.ansvarligEnhet) &&
            Objects.equals(bostedEnhet, that.bostedEnhet) &&
            Objects.equals(behandlendeEnhet, that.behandlendeEnhet) &&
            Objects.equals(kontrollFelt, that.kontrollFelt) &&
            Objects.equals(saksBehId, that.saksBehId) &&
            Objects.equals(referanse, that.referanse) &&
            erListeLik(perioder, that.perioder);
    }

    private boolean erListeLik(List<KravgrunnlagPeriode432> l1, List<KravgrunnlagPeriode432> l2) {
        if (l1 == null && l2 == null)
            return true;
        if (l1 == null || l2 == null)
            return false;
        return l1.size() == l2.size() && l2.containsAll(l1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eksternKravgrunnlagId, vedtakId, kravStatusKode, fagOmrådeKode, fagSystemId, vedtakFagSystemDato, omgjortVedtakId, gjelderVedtakId, gjelderType, utbetalesTilId, utbetGjelderType, hjemmelKode, beregnesRenter, ansvarligEnhet, bostedEnhet, behandlendeEnhet, kontrollFelt, saksBehId, referanse, perioder);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Kravgrunnlag431 kladd = new Kravgrunnlag431();

        public Builder medEksternKravgrunnlagId(String eksternKravgrunnlagId) {
            if (eksternKravgrunnlagId.length() >= 10) {
                throw new IllegalArgumentException("Forventer at kravgrunnlagId er maks 9 siffer. KravgrunnlagId=" + eksternKravgrunnlagId);
            }
            this.kladd.eksternKravgrunnlagId = eksternKravgrunnlagId;
            return this;
        }

        public Builder medVedtakId(Long vedtakId) {
            this.kladd.vedtakId = vedtakId;
            return this;
        }

        public Builder medKravStatusKode(KravStatusKode kravStatusKode) {
            this.kladd.kravStatusKode = kravStatusKode;
            return this;
        }

        public Builder medFagomraadeKode(FagOmrådeKode fagOmrådeKode) {
            this.kladd.fagOmrådeKode = fagOmrådeKode;
            return this;
        }

        public Builder medFagSystemId(String fagSystemId) {
            this.kladd.fagSystemId = fagSystemId;
            return this;
        }

        public Builder medVedtakFagSystemDato(LocalDate vedtakFagSystemDato) {
            this.kladd.vedtakFagSystemDato = vedtakFagSystemDato;
            return this;
        }

        public Builder medOmgjortVedtakId(Long omgjortVedtakId) {
            this.kladd.omgjortVedtakId = omgjortVedtakId;
            return this;
        }

        public Builder medGjelderVedtakId(String gjelderVedtakId) {
            this.kladd.gjelderVedtakId = gjelderVedtakId;
            return this;
        }

        public Builder medGjelderType(GjelderType gjelderType) {
            this.kladd.gjelderType = gjelderType;
            return this;
        }

        public Builder medUtbetalesTilId(String utbetalesTilId) {
            this.kladd.utbetalesTilId = utbetalesTilId;
            return this;
        }

        public Builder medUtbetIdType(GjelderType utbetGjelderType) {
            this.kladd.utbetGjelderType = utbetGjelderType;
            return this;
        }

        public Builder medHjemmelKode(String hjemmelKode) {
            this.kladd.hjemmelKode = hjemmelKode;
            return this;
        }

        public Builder medBeregnesRenter(String beregnesRenter) {
            this.kladd.beregnesRenter = beregnesRenter;
            return this;
        }

        public Builder medAnsvarligEnhet(String ansvarligEnhet) {
            this.kladd.ansvarligEnhet = ansvarligEnhet;
            return this;
        }

        public Builder medBostedEnhet(String bostedEnhet) {
            this.kladd.bostedEnhet = bostedEnhet;
            return this;
        }

        public Builder medBehandlendeEnhet(String behandlendeEnhet) {
            this.kladd.behandlendeEnhet = behandlendeEnhet;
            return this;
        }

        public Builder medFeltKontroll(String kontrollFelt) {
            this.kladd.kontrollFelt = kontrollFelt;
            return this;
        }

        public Builder medSaksBehId(String saksBehId) {
            this.kladd.saksBehId = saksBehId;
            return this;
        }

        public Builder medReferanse(Henvisning referanse) {
            this.kladd.referanse = referanse;
            return this;
        }

        public Kravgrunnlag431 build() {
            Objects.requireNonNull(this.kladd.eksternKravgrunnlagId, "eksternKravgrunnlagId");
            Objects.requireNonNull(this.kladd.vedtakId, "vedtakId");
            Objects.requireNonNull(this.kladd.kravStatusKode, "kravStatusKode");
            Objects.requireNonNull(this.kladd.fagOmrådeKode, "fagOmrådeKode");
            Objects.requireNonNull(this.kladd.utbetalesTilId, "utbetalesTilId");
            Objects.requireNonNull(this.kladd.utbetGjelderType, "utbetGjelderType");
            Objects.requireNonNull(this.kladd.fagSystemId, "fagSystemId");
            return kladd;
        }
    }

    @Override
    public String toString() {
        return "Kravgrunnlag431{" +
            "eksternKravgrunnlagId='" + eksternKravgrunnlagId + '\'' +
            ", vedtakId=" + vedtakId +
            ", kravStatusKode=" + kravStatusKode +
            ", fagOmrådeKode=" + fagOmrådeKode +
            ", fagSystemId='" + fagSystemId + '\'' +
            ", vedtakFagSystemDato=" + vedtakFagSystemDato +
            ", omgjortVedtakId=" + omgjortVedtakId +
            ", gjelderVedtakId='" + mask(gjelderVedtakId) + '\'' +
            ", gjelderType=" + gjelderType +
            ", utbetalesTilId='" + mask(utbetalesTilId) + '\'' +
            ", utbetGjelderType=" + utbetGjelderType +
            ", hjemmelKode='" + hjemmelKode + '\'' +
            ", beregnesRenter='" + beregnesRenter + '\'' +
            ", ansvarligEnhet='" + ansvarligEnhet + '\'' +
            ", bostedEnhet='" + bostedEnhet + '\'' +
            ", behandlendeEnhet='" + behandlendeEnhet + '\'' +
            ", kontrollFelt='" + kontrollFelt + '\'' +
            ", saksBehId='" + saksBehId + '\'' +
            ", referanse=" + referanse +
            ", perioder=" + perioder +
            '}';
    }
}
