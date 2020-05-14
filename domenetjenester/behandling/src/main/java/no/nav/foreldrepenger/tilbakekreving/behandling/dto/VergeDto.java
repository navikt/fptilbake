package no.nav.foreldrepenger.tilbakekreving.behandling.dto;

import java.time.LocalDate;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeType;
import no.nav.vedtak.util.InputValideringRegex;
import no.nav.vedtak.util.StringUtils;

public class VergeDto {

    @JsonProperty("gyldigFom")
    @NotNull
    private LocalDate fom;

    @JsonProperty("gyldigTom")
    @NotNull
    private LocalDate tom;

    @NotNull
    @Size(max = 1000)
    @Pattern(regexp = InputValideringRegex.NAVN)
    private String navn;

    @Digits(integer = 11, fraction = 0)
    private String fnr;

    @Pattern(regexp = "[\\d]{9}")
    private String organisasjonsnummer;

    @Valid
    @NotNull
    private VergeType vergeType;

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

    public String getNavn() {
        return navn;
    }

    public void setNavn(String navn) {
        this.navn = navn;
    }

    public String getFnr() {
        return fnr;
    }

    public void setFnr(String fnr) {
        this.fnr = fnr;
    }

    public String getOrganisasjonsnummer() {
        return organisasjonsnummer;
    }

    public void setOrganisasjonsnummer(String organisasjonsnummer) {
        this.organisasjonsnummer = organisasjonsnummer;
    }

    public VergeType getVergeType() {
        return vergeType;
    }

    public void setVergeType(VergeType vergeType) {
        this.vergeType = vergeType;
    }

    @AssertTrue(message = "organisasjonsnummer kan ikke være null for Advokat Verge")
    public boolean isFinnesOrganisasjonNummerForVergeAdvokat() { //NOSONAR
        return this.vergeType != VergeType.ADVOKAT || !StringUtils.nullOrEmpty(this.organisasjonsnummer);
    }

    @AssertTrue(message = "fnr kan ikke være null for alle Verge unntatt Advokat ")
    public boolean isFinnesOrganisasjonNummerForAnnenVerge() { //NOSONAR
        return this.vergeType == VergeType.ADVOKAT || !StringUtils.nullOrEmpty(this.fnr);
    }

}
