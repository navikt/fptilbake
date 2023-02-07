package no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør;

import java.util.Objects;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;

public class Adresseinfo {




    private String mottakerNavn;
    private PersonIdent personIdent;
    private String vergeOrganisasjonNummer;
    private String annenMottakerNavn;

    private Adresseinfo() {
    }

    public String getMottakerNavn() {
        return mottakerNavn;
    }

    public PersonIdent getPersonIdent() {
        return personIdent;
    }

    public String getVergeOrganisasjonNummer() {
        return vergeOrganisasjonNummer;
    }

    public String getAnnenMottakerNavn() {
        return annenMottakerNavn;
    }

    public void setAnnenMottakerNavn(String annenMottakerNavn) {
        this.annenMottakerNavn = annenMottakerNavn;
    }

    public static class Builder {
        private Adresseinfo kladd;

        public Builder(PersonIdent fnr, String mottakerNavn) {
            this.kladd = new Adresseinfo();
            this.kladd.personIdent = fnr;
            this.kladd.mottakerNavn = mottakerNavn;
        }

        public Builder medVergeOrganisasjonNummer(String vergeOrganisasjonNummer) {
            this.kladd.vergeOrganisasjonNummer = vergeOrganisasjonNummer;
            return this;
        }

        public Builder medAnnenMottakerNavn(String annenMottakerNavn) {
            this.kladd.annenMottakerNavn = annenMottakerNavn;
            return this;
        }

        public Adresseinfo build() {
            verifyStateForBuild();
            return kladd;
        }

        private void verifyStateForBuild() {
            Objects.requireNonNull(this.kladd.mottakerNavn, "mottakerNavn");
            Objects.requireNonNull(this.kladd.personIdent, "fnr");
        }
    }
}
