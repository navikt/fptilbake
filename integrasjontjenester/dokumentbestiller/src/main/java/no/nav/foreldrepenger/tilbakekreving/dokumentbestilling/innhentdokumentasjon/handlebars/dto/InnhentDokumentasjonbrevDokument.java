package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.innhentdokumentasjon.handlebars.dto;

import java.time.LocalDate;
import java.util.Objects;

public class InnhentDokumentasjonbrevDokument {

    private String fagsaktypeNavn;
    private String fritekstFraSaksbehandler;
    private LocalDate fristDato;
    private boolean finnesVerge;
    private String annenMottakerNavn;

    public String getFagsaktypeNavn() {
        return fagsaktypeNavn;
    }

    public void setFagsaktypeNavn(String fagsaktypeNavn) {
        this.fagsaktypeNavn = fagsaktypeNavn;
    }

    public String getFritekstFraSaksbehandler() {
        return fritekstFraSaksbehandler;
    }

    public void setFritekstFraSaksbehandler(String fritekstFraSaksbehandler) {
        this.fritekstFraSaksbehandler = fritekstFraSaksbehandler;
    }

    public LocalDate getFristDato() {
        return fristDato;
    }

    public void setFristDato(LocalDate fristDato) {
        this.fristDato = fristDato;
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

    public void valider() {
        Objects.requireNonNull(getFagsaktypeNavn(), "fagsaktypeNavn kan ikke være null");
        Objects.requireNonNull(getFritekstFraSaksbehandler(), "fritekst kan ikke være null");
        Objects.requireNonNull(getFristDato(), "fristDato kan ikke være null");
        if(isFinnesVerge()){
            Objects.requireNonNull(getAnnenMottakerNavn(), "annenMottakerNavn kan ikke være null");
        }
    }
}
