package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.handlebars.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.LocalDateTilLangtNorskFormatSerialiserer;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.dto.BaseDokument;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.dto.periode.HbPeriode;

public class VarselbrevDokument extends BaseDokument {

    private Long beløp;

    @JsonSerialize(using = LocalDateTilLangtNorskFormatSerialiserer.class)
    private LocalDate endringsdato;

    @JsonProperty("feilutbetaltePerioder")
    private List<HbPeriode> feilutbetaltePerioder;
    private String varseltekstFraSaksbehandler;

    @JsonSerialize(using = LocalDateTilLangtNorskFormatSerialiserer.class)
    private LocalDate fristdatoForTilbakemelding;
    private HbPeriode datoerHvisSammenhengendePeriode;

    @JsonSerialize(using = LocalDateTilLangtNorskFormatSerialiserer.class)
    private LocalDate varsletDato;
    private Long varsletBeløp;
    private boolean finnesVerge;
    private String annenMottakerNavn;

    private boolean isKorrigert;

    public Long getBeløp() {
        return beløp;
    }

    public void setBeløp(Long beløp) {
        this.beløp = beløp;
    }

    public LocalDate getEndringsdato() {
        return endringsdato;
    }

    public void setEndringsdato(LocalDate endringsdato) {
        this.endringsdato = endringsdato;
    }

    public List<HbPeriode> getFeilutbetaltePerioder() {
        return feilutbetaltePerioder;
    }

    public void setFeilutbetaltePerioder(List<HbPeriode> feilutbetaltePerioder) {
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

    public HbPeriode getDatoerHvisSammenhengendePeriode() {
        return datoerHvisSammenhengendePeriode;
    }

    public void setDatoerHvisSammenhengendePeriode(HbPeriode datoerHvisSammenhengendePeriode) {
        this.datoerHvisSammenhengendePeriode = datoerHvisSammenhengendePeriode;
    }

    public LocalDate getVarsletDato() {
        return varsletDato;
    }

    public void setVarsletDato(LocalDate varsletDato) {
        this.varsletDato = varsletDato;
    }

    public Long getVarsletBeløp() {
        return varsletBeløp;
    }

    public void setVarsletBeløp(Long varsletBeløp) {
        this.varsletBeløp = varsletBeløp;
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

    public boolean isKorrigert() {
        return isKorrigert;
    }

    public void setKorrigert(boolean korrigert) {
        isKorrigert = korrigert;
    }

    public boolean isYtelseUtenSkatt() {
        return erYtelseType(FagsakYtelseType.ENGANGSTØNAD);
    }

    public boolean isYtelseMedSkatt() {
        return erYtelseType(FagsakYtelseType.FORELDREPENGER) || erYtelseType(FagsakYtelseType.SVANGERSKAPSPENGER) || erYtelseType(FagsakYtelseType.FRISINN);
    }

    @JsonProperty("skal-vise-renteinformasjon")
    public boolean isSkalViseRenteinformasjon() {
        return erYtelseType(FagsakYtelseType.FORELDREPENGER) || erYtelseType(FagsakYtelseType.SVANGERSKAPSPENGER) || erYtelseType(FagsakYtelseType.ENGANGSTØNAD);
    }

    public void valider() {
        Objects.requireNonNull(beløp, "totalbeløp fra fpoppdrag/oppdragssystemet");
        Objects.requireNonNull(endringsdato, "endringsdato");
        Objects.requireNonNull(getFagsaktypeNavn(), "fagtypenavn/ytelsenavn");
        Objects.requireNonNull(fristdatoForTilbakemelding, "fristdato for tilbakemelding");
        Objects.requireNonNull(feilutbetaltePerioder, "feilutbetalte perioder");

        if (isEngangsstønad()) {
            Objects.requireNonNull(datoerHvisSammenhengendePeriode, "utbetalingsdato/fom-dato for engangsstønad");
        } else if (isForeldrepenger() || isSvangerskapspenger()) {
            if (feilutbetaltePerioder.size() == 1) {
                Objects.requireNonNull(datoerHvisSammenhengendePeriode, "datoer for sammenhengende periode");
            } else if (feilutbetaltePerioder.size() > 1) {
                for (HbPeriode feilutbetaltPeriode : feilutbetaltePerioder) {
                    Objects.requireNonNull(feilutbetaltPeriode.getFom(), "fraogmed-dato for feilutbetalingsperiode");
                    Objects.requireNonNull(feilutbetaltPeriode.getTom(), "tilogmed-dato for feilutbetalingsperiode");
                }
            }
        }

        if(isKorrigert()){
            Objects.requireNonNull(varsletDato, "varsletDato");
            Objects.requireNonNull(varsletBeløp, "varsletBelop");
        }

        if(finnesVerge){
            Objects.requireNonNull(annenMottakerNavn,"annenMottakerNavn");
        }
    }

}
