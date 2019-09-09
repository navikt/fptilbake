package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.handlebars.dto;

import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class VarselbrevDokument extends BaseDokument {

    private Long belop;
    private LocalDate endringsdato;
    private List<Periode> feilutbetaltePerioder;
    private String varseltekstFraSaksbehandler;
    private LocalDate fristdatoForTilbakemelding;
    private Periode datoerHvisSammenhengendePeriode;
    private String kontakttelefonnummer;

    public String getKontakttelefonnummer() {
        return kontakttelefonnummer;
    }

    public void setKontakttelefonnummer(String kontakttelefonnummer) {
        this.kontakttelefonnummer = kontakttelefonnummer;
    }

    public Long getBelop() {
        return belop;
    }

    public void setBelop(Long belop) {
        this.belop = belop;
    }

    public LocalDate getEndringsdato() {
        return endringsdato;
    }

    public void setEndringsdato(LocalDate endringsdato) {
        this.endringsdato = endringsdato;
    }

    public List<Periode> getFeilutbetaltePerioder() {
        return feilutbetaltePerioder;
    }

    public void setFeilutbetaltePerioder(List<Periode> feilutbetaltePerioder) {
        this.feilutbetaltePerioder = feilutbetaltePerioder;
    }

    public String getVarseltekstFraSaksbehandler() {
        return varseltekstFraSaksbehandler;
    }

    public void setVarseltekstFraSaksbehandler(String varseltekstFraSaksbehandler) {
        this.varseltekstFraSaksbehandler = varseltekstFraSaksbehandler;
    }

    public LocalDate getFristdatoForTilbakemelding() {
        return fristdatoForTilbakemelding;
    }

    public void setFristdatoForTilbakemelding(LocalDate fristdatoForTilbakemelding) {
        this.fristdatoForTilbakemelding = fristdatoForTilbakemelding;
    }

    public Periode getDatoerHvisSammenhengendePeriode() {
        return datoerHvisSammenhengendePeriode;
    }

    public void setDatoerHvisSammenhengendePeriode(Periode datoerHvisSammenhengendePeriode) {
        this.datoerHvisSammenhengendePeriode = datoerHvisSammenhengendePeriode;
    }

    public void valider() {
        Objects.requireNonNull(belop, "totalbeløp fra fpoppdrag/oppdragssystemet");
        Objects.requireNonNull(endringsdato, "endringsdato");
        Objects.requireNonNull(getFagsaktypeNavn(), "fagtypenavn/ytelsenavn");
        Objects.requireNonNull(fristdatoForTilbakemelding, "fristdato for tilbakemelding");
        Objects.requireNonNull(kontakttelefonnummer, "kontakttelefonnummer");
        Objects.requireNonNull(feilutbetaltePerioder, "feilutbetalte perioder");

        if (isEngangsstonad()) {
            Objects.requireNonNull(datoerHvisSammenhengendePeriode, "utbetalingsdato/fom-dato for engangsstønad");
        } else if (isForeldrepenger() || isSvangerskapspenger()) {
            if (feilutbetaltePerioder.size() == 1) {
                Objects.requireNonNull(datoerHvisSammenhengendePeriode, "datoer for sammenhengende periode");
            } else if (feilutbetaltePerioder.size() > 1) {
                for (Periode feilutbetaltPeriode : feilutbetaltePerioder) {
                    Objects.requireNonNull(feilutbetaltPeriode.getFom(), "fraogmed-dato for feilutbetalingsperiode");
                    Objects.requireNonNull(feilutbetaltPeriode.getTom(), "tilogmed-dato for feilutbetalingsperiode");
                }
            }
        }
    }

}
