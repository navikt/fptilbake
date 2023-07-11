package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge;

import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

@Entity(name = "VergeEntitet")
@Table(name = "VERGE")
public class VergeEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_VERGE")
    private Long id;

    @Column(name = "aktoer_id")
    private AktørId vergeAktørId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "fom", column = @Column(name = "gyldig_fom", nullable = false)),
            @AttributeOverride(name = "tom", column = @Column(name = "gyldig_tom", nullable = false))
    })
    private Periode gyldigPeriode; //NOSONAR

    @Convert(converter = VergeType.KodeverdiConverter.class)
    @Column(name = "verge_type")
    private VergeType vergeType = VergeType.UDEFINERT;

    @Column(name = "orgnr")
    private String organisasjonsnummer;

    @Column(name = "navn", nullable = false)
    private String navn;

    @Column(name = "kilde", nullable = false)
    private String kilde;

    @Column(name = "begrunnelse", nullable = false)
    private String begrunnelse;

    VergeEntitet() {
        // Hibernate
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VergeEntitet that = (VergeEntitet) o;
        return
                Objects.equals(vergeAktørId, that.vergeAktørId) &&
                        Objects.equals(gyldigPeriode, that.gyldigPeriode) &&
                        Objects.equals(vergeType, that.vergeType) &&
                        Objects.equals(navn, that.navn) &&
                        Objects.equals(kilde, that.kilde);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gyldigPeriode, vergeType, navn, kilde);
    }

    public VergeType getVergeType() {
        return vergeType;
    }

    public LocalDate getGyldigFom() {
        return gyldigPeriode.getFom();
    }

    public LocalDate getGyldigTom() {
        return gyldigPeriode.getTom();
    }

    public AktørId getVergeAktørId() {
        return vergeAktørId;
    }

    public Long getId() {
        return id;
    }

    public String getOrganisasjonsnummer() {
        return organisasjonsnummer;
    }

    public String getNavn() {
        return navn;
    }

    public String getKilde() {
        return kilde;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private VergeEntitet kladd = new VergeEntitet();

        public Builder medVergeAktørId(AktørId aktørId) {
            this.kladd.vergeAktørId = aktørId;
            return this;
        }

        public Builder medGyldigPeriode(LocalDate fom, LocalDate tom) {
            if (fom != null && tom != null) {
                this.kladd.gyldigPeriode = Periode.of(fom, tom);
            }
            return this;
        }

        public Builder medVergeType(VergeType vergeType) {
            this.kladd.vergeType = vergeType;
            return this;
        }

        public Builder medOrganisasjonnummer(String organisasjonsnummer) {
            this.kladd.organisasjonsnummer = organisasjonsnummer;
            return this;
        }

        public Builder medNavn(String navn) {
            this.kladd.navn = navn;
            return this;
        }

        public Builder medKilde(String kilde) {
            this.kladd.kilde = kilde;
            return this;
        }

        public Builder medBegrunnelse(String begrunnelse) {
            this.kladd.begrunnelse = begrunnelse;
            return this;
        }

        public VergeEntitet build() {
            Objects.requireNonNull(this.kladd.vergeType, "vergeType");
            Objects.requireNonNull(this.kladd.kilde, "kilde");
            Objects.requireNonNull(this.kladd.navn, "navn");
            Objects.requireNonNull(this.kladd.begrunnelse, "begrunnelse");

            if (this.kladd.vergeAktørId == null && (this.kladd.organisasjonsnummer == null || this.kladd.organisasjonsnummer.isEmpty())) {
                throw new IllegalArgumentException("Organisasjonsnummer eller vergeAktørId må finnes for verge organisasjon");

            }
            return kladd;
        }
    }
}
