package no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;

public class Personinfo {

    private AktørId aktørId;
    private String navn;
    private PersonIdent personIdent;
    private LocalDate fødselsdato;
    private LocalDate dødsdato;
    private SivilstandType sivilstand;

    private Personinfo() {
    }

    public AktørId getAktørId() {
        return aktørId;
    }

    public PersonIdent getPersonIdent() {
        return personIdent;
    }

    public String getNavn() {
        return navn;
    }

    public LocalDate getFødselsdato() {
        return fødselsdato;
    }

    public LocalDate getDødsdato() {
        return dødsdato;
    }

    public SivilstandType getSivilstandType() {
        return sivilstand;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<aktørId=" + aktørId + ">";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Personinfo personinfoMal;

        public Builder() {
            personinfoMal = new Personinfo();
        }

        public Builder medAktørId(AktørId aktørId) {
            personinfoMal.aktørId = aktørId;
            return this;
        }

        public Builder medNavn(String navn) {
            personinfoMal.navn = navn;
            return this;
        }

        public Builder medPersonIdent(PersonIdent fnr) {
            personinfoMal.personIdent = fnr;
            return this;
        }

        public Builder medFødselsdato(LocalDate fødselsdato) {
            personinfoMal.fødselsdato = fødselsdato;
            return this;
        }

        public Builder medDødsdato(LocalDate dødsdato) {
            personinfoMal.dødsdato = dødsdato;
            return this;
        }

        public Builder medSivilstandType(SivilstandType sivilstandType) {
            personinfoMal.sivilstand = sivilstandType;
            return this;
        }

        public Personinfo build() {
            requireNonNull(personinfoMal.aktørId, "Navbruker må ha aktørId");
            requireNonNull(personinfoMal.personIdent, "Navbruker må ha fødselsnummer");
            requireNonNull(personinfoMal.navn, "Navbruker må ha navn");
            return personinfoMal;
        }

    }

}
