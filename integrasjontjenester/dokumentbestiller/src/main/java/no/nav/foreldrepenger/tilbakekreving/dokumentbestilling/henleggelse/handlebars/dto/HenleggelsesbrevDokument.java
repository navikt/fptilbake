package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.henleggelse.handlebars.dto;

import java.time.LocalDate;
import java.util.Objects;

public class HenleggelsesbrevDokument extends BaseDokument{

    private String avsenderEnhetNavn;
    private LocalDate varsletDato;

    public String getAvsenderEnhetNavn() {
        return avsenderEnhetNavn;
    }

    public void setAvsenderEnhetNavn(String avsenderEnhetNavn) {
        this.avsenderEnhetNavn = avsenderEnhetNavn;
    }

    public LocalDate getVarsletDato() {
        return varsletDato;
    }

    public void setVarsletDato(LocalDate varsletDato) {
        this.varsletDato = varsletDato;
    }

    public void valider() {
        Objects.requireNonNull(getFagsaktypeNavn(), "fagsaktypeNavn kan ikke være null");
        Objects.requireNonNull(avsenderEnhetNavn, "avsenderEnhetNavn kan ikke være null");
        Objects.requireNonNull(varsletDato, "varsletDato kan ikke være null");
    }
}
