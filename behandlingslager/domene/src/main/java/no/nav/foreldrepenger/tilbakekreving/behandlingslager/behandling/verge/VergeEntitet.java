package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkBaseEntitet;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

@Entity(name = "Verge")
@Table(name = "VERGE")
public class VergeEntitet extends KodeverkBaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_VERGE")
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "bruker_id")
    private NavBruker bruker;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "fom", column = @Column(name = "gyldig_fom")),
        @AttributeOverride(name = "tom", column = @Column(name = "gyldig_tom"))
    })
    private Periode gyldigPeriode;

    @Convert(converter = VergeType.KodeverdiConverter.class)
    @Column(name = "verge_type", nullable = false)
    private VergeType vergeType = VergeType.UDEFINERT;


    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "organisasjon_id")
    private VergeOrganisasjonEntitet vergeOrganisasjon;

    VergeEntitet() {
        // Hibernate
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VergeEntitet that = (VergeEntitet) o;
        return
            Objects.equals(bruker, that.bruker) &&
                Objects.equals(gyldigPeriode, that.gyldigPeriode) &&
                Objects.equals(vergeType, that.vergeType) &&
                Objects.equals(vergeOrganisasjon, that.vergeOrganisasjon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bruker, gyldigPeriode, vergeType);
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

    public NavBruker getBruker() {
        return bruker;
    }

    public Long getId() {
        return id;
    }

    public Optional<VergeOrganisasjonEntitet> getVergeOrganisasjon() {
        return Optional.ofNullable(vergeOrganisasjon);
    }

    public void setVergeOrganisasjon(VergeOrganisasjonEntitet vergeOrganisasjon) {
        this.vergeOrganisasjon = vergeOrganisasjon;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private VergeEntitet kladd = new VergeEntitet();

        public Builder medBruker(NavBruker navBruker) {
            this.kladd.bruker = navBruker;
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

        public VergeEntitet build() {
            Objects.requireNonNull(this.kladd.bruker, "bruker");
            Objects.requireNonNull(this.kladd.vergeType, "vergeType");
            return kladd;
        }
    }
}
