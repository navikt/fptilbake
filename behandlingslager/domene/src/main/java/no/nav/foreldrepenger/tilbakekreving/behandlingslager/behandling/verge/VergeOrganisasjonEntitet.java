package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkBaseEntitet;

@Entity(name = "VergeOrganisasjon")
@Table(name = "VERGE_ORGANISASJON")
public class VergeOrganisasjonEntitet extends KodeverkBaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_VERGE_ORGANISASJON")
    private Long id;

    @Column(name = "orgnr")
    private String organisasjonsnummer;

    @Column(name = "navn")
    private String navn;

    @OneToOne(mappedBy = "vergeOrganisasjon")
    private VergeEntitet verge;

    public VergeOrganisasjonEntitet() {
        //Hibernate
    }

    // deep copy
    VergeOrganisasjonEntitet(VergeOrganisasjonEntitet vergeOrganisasjon, VergeEntitet verge) {
        this.organisasjonsnummer = vergeOrganisasjon.getOrganisasjonsnummer();
        this.navn = vergeOrganisasjon.getNavn();
        this.verge = verge;
    }

    public String getOrganisasjonsnummer() {
        return organisasjonsnummer;
    }

    public void setOrganisasjonsnummer(String organisasjonsnummer) {
        this.organisasjonsnummer = organisasjonsnummer;
    }


    public String getNavn() {
        return navn;
    }

    public void setNavn(String navn) {
        this.navn = navn;
    }


    public VergeEntitet getVerge() {
        return verge;
    }

    public void setVerge(VergeEntitet verge) {
        this.verge = verge;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VergeOrganisasjonEntitet entitet = (VergeOrganisasjonEntitet) o;
        return Objects.equals(organisasjonsnummer, entitet.organisasjonsnummer) &&
            Objects.equals(navn, entitet.navn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organisasjonsnummer, navn);
    }

    public static Builder builder() {return new Builder();}

    public static class Builder {
        private VergeOrganisasjonEntitet kladd = new VergeOrganisasjonEntitet();

        public Builder medOrganisasjonnummer(String organisasjonnummer) {
            this.kladd.organisasjonsnummer = organisasjonnummer;
            return this;
        }

        public Builder medNavn(String navn) {
            this.kladd.navn = navn;
            return this;
        }

        public Builder medVerge(VergeEntitet vergeEntitet) {
            this.kladd.verge = vergeEntitet;
            return this;
        }

        public VergeOrganisasjonEntitet build() {
            Objects.requireNonNull(this.kladd.organisasjonsnummer, "organisasjonsnummer");
            Objects.requireNonNull(this.kladd.verge, "verge");
            return kladd;
        }

    }

}
