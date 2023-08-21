package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto;

import java.time.LocalDate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktKodeDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeType;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.AppAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.util.InputValideringRegex;

@JsonTypeName(AksjonspunktKodeDefinisjon.AVKLAR_VERGE)
public class AvklartVergeDto extends BekreftetAksjonspunktDto {

    @JsonProperty("gyldigFom")
    @NotNull
    private LocalDate fom;

    @JsonProperty("gyldigTom")
    @NotNull
    private LocalDate tom;

    @JsonProperty("navn")
    @NotNull
    @Size(max = 1000)
    @Pattern(regexp = InputValideringRegex.NAVN)
    private String navn;

    @JsonProperty("fnr")
    @Digits(integer = 11, fraction = 0)
    private String fnr;

    @JsonProperty("organisasjonsnummer")
    @Pattern(regexp = "[\\d]{9}")
    private String organisasjonsnummer;

    @JsonProperty("vergeType")
    @Valid
    @NotNull
    private VergeType vergeType;

    @JsonProperty("begrunnelse")
    @NotNull
    @Size(max = 4000)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String begrunnelse;

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

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public void setBegrunnelse(String begrunnelse) {
        this.begrunnelse = begrunnelse;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        AbacDataAttributter abacDataAttributter = AbacDataAttributter.opprett();
        if (this.fnr != null) {
            abacDataAttributter.leggTil(AppAbacAttributtType.FNR, this.fnr);
        }
        return abacDataAttributter;
    }

}
