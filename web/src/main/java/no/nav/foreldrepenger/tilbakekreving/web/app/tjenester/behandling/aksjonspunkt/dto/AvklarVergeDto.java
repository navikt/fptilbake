package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto;

import java.time.LocalDate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktKodeDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeType;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.AppAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.util.InputValideringRegex;

@JsonTypeName(AksjonspunktKodeDefinisjon.AVKLAR_VERGE)
public class AvklarVergeDto extends BekreftetAksjonspunktDto {

    @NotNull
    private LocalDate gyldigFom;

    @NotNull
    private LocalDate gyldigTom;

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

    @Size(max = 100)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String begrunnelse;

    public LocalDate getGyldigFom() {
        return gyldigFom;
    }

    public void setGyldigFom(LocalDate gyldigFom) {
        this.gyldigFom = gyldigFom;
    }

    public LocalDate getGyldigTom() {
        return gyldigTom;
    }

    public void setGyldigTom(LocalDate gyldigTom) {
        this.gyldigTom = gyldigTom;
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