package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.util.InputValideringRegex;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class HentForhåndsvisningVarselbrevDto implements AbacDto {

    @NotNull
    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long behandlingId;

    @NotNull
    @Digits(integer = 18, fraction = 0)
    private String saksnummer;

    @Size(max = 1500, message = "Varseltekst er for lang")
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String varseltekst;

    public String getSaksnummer() {
        return saksnummer;
    }

    public void setSaksnummer(String saksnummer) {
        this.saksnummer = saksnummer;
    }

    public Long getBehandlingId() {
        return behandlingId;
    }

    public void setBehandlingId(Long behandlingId) {
        this.behandlingId = behandlingId;
    }

    public String getVarseltekst() {
        return varseltekst;
    }

    public void setVarseltekst(String varseltekst) {
        this.varseltekst = varseltekst;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.SAKSNUMMER, getSaksnummer());
    }
}
