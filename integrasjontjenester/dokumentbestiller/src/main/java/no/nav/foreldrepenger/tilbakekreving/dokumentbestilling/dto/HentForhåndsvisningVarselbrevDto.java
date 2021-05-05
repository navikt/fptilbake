package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto;

import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

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
