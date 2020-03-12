package no.nav.foreldrepenger.tilbakekreving.kontrakter.vedtak;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VedtakPeriode {

    private LocalDate fom;
    private LocalDate tom;
    private String hendelseTypeTekst;
    private String hendelseUndertypeTekst;
    private boolean harBruktSjetteLedd;
    private Aktsomhet aktsomhet;
    @JsonProperty(value = "saerligeGrunner")
    private SærligeGrunner særligeGrunner;
    @JsonProperty(value = "feilutbetaltBeloep")
    private BigDecimal feilutbetaltBeløp;
    @JsonProperty(value = "tilbakekrevesBruttoBeloep")
    private BigDecimal tilbakekrevesBruttoBeløp;
    @JsonProperty(value = "renterBeloep")
    private BigDecimal renterBeløp;

    public LocalDate getFom() {
        return fom;
    }

    public void setFom(LocalDate fom) {
        this.fom = fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public void setTom(LocalDate tom) {
        this.tom = tom;
    }

    public String getHendelseTypeTekst() {
        return hendelseTypeTekst;
    }

    public void setHendelseTypeTekst(String hendelseTypeTekst) {
        this.hendelseTypeTekst = hendelseTypeTekst;
    }

    public String getHendelseUndertypeTekst() {
        return hendelseUndertypeTekst;
    }

    public void setHendelseUndertypeTekst(String hendelseUndertypeTekst) {
        this.hendelseUndertypeTekst = hendelseUndertypeTekst;
    }

    public boolean isHarBruktSjetteLedd() {
        return harBruktSjetteLedd;
    }

    public void setHarBruktSjetteLedd(boolean harBruktSjetteLedd) {
        this.harBruktSjetteLedd = harBruktSjetteLedd;
    }

    public Aktsomhet getAktsomhet() {
        return aktsomhet;
    }

    public void setAktsomhet(Aktsomhet aktsomhet) {
        this.aktsomhet = aktsomhet;
    }

    public SærligeGrunner getSærligeGrunner() {
        return særligeGrunner;
    }

    public void setSærligeGrunner(SærligeGrunner særligeGrunner) {
        this.særligeGrunner = særligeGrunner;
    }

    public BigDecimal getFeilutbetaltBeløp() {
        return feilutbetaltBeløp;
    }

    public void setFeilutbetaltBeløp(BigDecimal feilutbetaltBeløp) {
        this.feilutbetaltBeløp = feilutbetaltBeløp;
    }

    public BigDecimal getTilbakekrevesBruttoBeløp() {
        return tilbakekrevesBruttoBeløp;
    }

    public void setTilbakekrevesBruttoBeløp(BigDecimal tilbakekrevesBruttoBeløp) {
        this.tilbakekrevesBruttoBeløp = tilbakekrevesBruttoBeløp;
    }

    public BigDecimal getRenterBeløp() {
        return renterBeløp;
    }

    public void setRenterBeløp(BigDecimal renterBeløp) {
        this.renterBeløp = renterBeløp;
    }
}
