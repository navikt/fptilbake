package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.verge.dto;

import java.time.LocalDate;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeType;

public class VergeDto {

    private String navn;
    private String fnr;
    private LocalDate gyldigFom;
    private LocalDate gyldigTom;
    private VergeType vergeType;
    private String organisasjonsnummer;
    private String begrunnelse;

    public VergeDto() { //NOSONAR
    }

    public void setNavn(String navn) {
        this.navn = navn;
    }

    public void setFnr(String fnr) {
        this.fnr = fnr;
    }

    public void setGyldigFom(LocalDate gyldigFom) {
        this.gyldigFom = gyldigFom;
    }

    public void setGyldigTom(LocalDate gyldigTom) {
        this.gyldigTom = gyldigTom;
    }

    public void setVergeType(VergeType vergeType) {
        this.vergeType = vergeType;
    }

    public void setOrganisasjonsnummer(String organisasjonsnummer) {
        this.organisasjonsnummer = organisasjonsnummer;
    }

    public String getNavn() {
        return navn;
    }

    public String getFnr() {
        return fnr;
    }

    public LocalDate getGyldigFom() {
        return gyldigFom;
    }

    public LocalDate getGyldigTom() {
        return gyldigTom;
    }

    public VergeType getVergeType() {
        return vergeType;
    }

    public String getOrganisasjonsnummer() {
        return organisasjonsnummer;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public void setBegrunnelse(String begrunnelse) {
        this.begrunnelse = begrunnelse;
    }
}

