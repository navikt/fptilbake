package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;

import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.TilbakekrevingAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public class FpsakUuidDto implements AbacDto {

    @NotNull
    @Valid
    private UUID uuid;

    public FpsakUuidDto(String uuid) {
        this.uuid = UUID.fromString(uuid);
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett().leggTil(TilbakekrevingAbacAttributtType.YTELSEBEHANDLING_UUID, uuid);
    }
}
