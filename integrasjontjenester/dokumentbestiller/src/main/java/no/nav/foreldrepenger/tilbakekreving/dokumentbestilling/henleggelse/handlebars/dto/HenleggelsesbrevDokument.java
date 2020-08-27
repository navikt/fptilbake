package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.henleggelse.handlebars.dto;

import java.time.LocalDate;
import java.util.Objects;

public class HenleggelsesbrevDokument {

    private String fagsaktypeNavn;
    private LocalDate varsletDato;
    private boolean finnesVerge;
    private String annenMottakerNavn;
    private boolean tilbakekrevingRevurdering;
    private String fritekstFraSaksbehandler;

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


    public boolean isFinnesVerge() {
        return finnesVerge;
    }

    public void setFinnesVerge(boolean finnesVerge) {
        this.finnesVerge = finnesVerge;
    }

    public String getAnnenMottakerNavn() {
        return annenMottakerNavn;
    }

    public void setAnnenMottakerNavn(String annenMottakerNavn) {
        this.annenMottakerNavn = annenMottakerNavn;
    }

    public boolean isTilbakekrevingRevurdering() {
        return tilbakekrevingRevurdering;
    }

    public void setTilbakekrevingRevurdering(boolean tilbakekrevingRevurdering) {
        this.tilbakekrevingRevurdering = tilbakekrevingRevurdering;
    }

    public String getFritekstFraSaksbehandler() {
        return fritekstFraSaksbehandler;
    }

    public void setFritekstFraSaksbehandler(String fritekstFraSaksbehandler) {
        this.fritekstFraSaksbehandler = fritekstFraSaksbehandler;
    }

    public void valider() {
        Objects.requireNonNull(getFagsaktypeNavn(), "fagsaktypeNavn kan ikke være null");
        if(isTilbakekrevingRevurdering()){
            Objects.requireNonNull(getFritekstFraSaksbehandler(), "fritekst kan ikke være null");
        }else {
            Objects.requireNonNull(getVarsletDato(), "varsletDato kan ikke være null");
        }
        if(isFinnesVerge()){
            Objects.requireNonNull(getAnnenMottakerNavn(), "annenMottakerNavn kan ikke være null");
        }
    }
}
