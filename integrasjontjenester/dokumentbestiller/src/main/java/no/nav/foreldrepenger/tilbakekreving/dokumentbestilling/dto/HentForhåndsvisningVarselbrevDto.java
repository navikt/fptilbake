package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto;

import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.util.InputValideringRegex;

public class HentForhåndsvisningVarselbrevDto implements AbacDto {

    @NotNull
    @Valid
    private UUID behandlingUuId;

    @NotNull
    @Digits(integer = 18, fraction = 0)
    private String saksnummer;

    @Size(max = 1500, message = "Varseltekst er for lang")
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String varseltekst;

    @NotNull
    @Size(max = 100, message = "FagsakYtelseType er for lang")
    @Pattern(regexp = InputValideringRegex.KODEVERK)
    private String fagsakYtelseType;

    public String getSaksnummer() {
        return saksnummer;
    }

    public void setSaksnummer(String saksnummer) {
        this.saksnummer = saksnummer;
    }

    public UUID getBehandlingUuId() {
        return behandlingUuId;
    }

    public void setBehandlingUuId(UUID behandlingUuId) {
        this.behandlingUuId = behandlingUuId;
    }

    public String getVarseltekst() {
        return varseltekst;
    }

    public void setVarseltekst(String varseltekst) {
        this.varseltekst = varseltekst;
    }

    public String getFagsakYtelseType() {
        return fagsakYtelseType;
    }

    public void setFagsakYtelseType(String fagsakYtelseType) {
        this.fagsakYtelseType = fagsakYtelseType;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.SAKSNUMMER, getSaksnummer());
    }
}
