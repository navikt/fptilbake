package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto;

import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.TilbakekrevingAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.util.InputValideringRegex;

public class HentForhåndsvisningFritekstVedtaksbrevDto implements AbacDto {

    @NotNull
    @Valid
    private UUID behandlingUuid;

    @Size(max = 10000, message = "fritekst er for lang")
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String fritekst;

    public UUID getBehandlingUuid() {
        return behandlingUuid;
    }

    public void setBehandlingUuid(UUID behandlingUuid) {
        this.behandlingUuid = behandlingUuid;
    }

    public String getFritekst() {
        return fritekst;
    }

    public void setFritekst(String fritekst) {
        this.fritekst = fritekst;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett().leggTil(TilbakekrevingAbacAttributtType.YTELSEBEHANDLING_UUID, behandlingUuid);
    }
}
