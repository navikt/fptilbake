package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.henleggelse;

import java.time.LocalDate;

import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.BrevMetadata;

public class HenleggelsesbrevSamletInfo {

    private BrevMetadata brevMetadata;
    private LocalDate varsletDato;

    public BrevMetadata getBrevMetadata() {
        return brevMetadata;
    }

    public void setBrevMetadata(BrevMetadata brevMetadata) {
        this.brevMetadata = brevMetadata;
    }

    public LocalDate getVarsletDato() {
        return varsletDato;
    }

    public void setVarsletDato(LocalDate varsletDato) {
        this.varsletDato = varsletDato;
    }
}
