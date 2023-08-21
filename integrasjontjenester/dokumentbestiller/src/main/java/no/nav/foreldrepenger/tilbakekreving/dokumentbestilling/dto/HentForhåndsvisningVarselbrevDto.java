package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.vedtak.util.InputValideringRegex;

public class HentForhåndsvisningVarselbrevDto {

    @NotNull
    @Valid
    private UUID behandlingUuid;

    @Size(max = 1500, message = "Varseltekst er for lang")
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String varseltekst;

    @NotNull
    @Valid
    private FagsakYtelseType fagsakYtelseType;

    public UUID getBehandlingUuid() {
        return behandlingUuid;
    }

    public void setBehandlingUuid(UUID behandlingUuid) {
        this.behandlingUuid = behandlingUuid;
    }

    public String getVarseltekst() {
        return varseltekst;
    }

    public void setVarseltekst(String varseltekst) {
        this.varseltekst = varseltekst;
    }

    public FagsakYtelseType getFagsakYtelseType() {
        return fagsakYtelseType;
    }

    public void setFagsakYtelseType(FagsakYtelseType fagsakYtelseType) {
        this.fagsakYtelseType = fagsakYtelseType;
    }
}
