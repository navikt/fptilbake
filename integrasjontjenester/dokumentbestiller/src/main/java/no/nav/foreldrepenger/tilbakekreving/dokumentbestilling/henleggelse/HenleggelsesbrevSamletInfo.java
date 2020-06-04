package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.henleggelse;

import java.time.LocalDate;

import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.BrevMetadata;

public class HenleggelsesbrevSamletInfo {

    private BrevMetadata brevMetadata;
    private LocalDate varsletDato;
    private boolean finnesVerge;

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

    public boolean isFinnesVerge() {
        return finnesVerge;
    }

    public void setFinnesVerge(boolean finnesVerge) {
        this.finnesVerge = finnesVerge;
    }
}
