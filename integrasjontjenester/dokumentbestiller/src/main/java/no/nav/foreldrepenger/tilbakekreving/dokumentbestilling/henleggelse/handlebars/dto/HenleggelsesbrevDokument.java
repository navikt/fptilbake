package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.henleggelse.handlebars.dto;

import java.time.LocalDate;
import java.util.Objects;

import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.Lokale;

public class HenleggelsesbrevDokument {

    private String fagsaktypeNavn;
    private LocalDate varsletDato;
    private Lokale lokale;

    public String getFagsaktypeNavn() {
        return fagsaktypeNavn;
    }

    public void setFagsaktypeNavn(String fagsaktypeNavn) {
        this.fagsaktypeNavn = fagsaktypeNavn;
    }

    public LocalDate getVarsletDato() {
        return varsletDato;
    }

    public void setVarsletDato(LocalDate varsletDato) {
        this.varsletDato = varsletDato;
    }

    public String getLokale() {
        return lokale.getTekst();
    }

    public void setLokale(Lokale lokale) {
        this.lokale = lokale;
    }

    public void valider() {
        Objects.requireNonNull(getFagsaktypeNavn(), "fagsaktypeNavn kan ikke være null");
        Objects.requireNonNull(getVarsletDato(), "varsletDato kan ikke være null");
        Objects.requireNonNull(getLokale(), "lokale kan ikke være null");
    }
}
