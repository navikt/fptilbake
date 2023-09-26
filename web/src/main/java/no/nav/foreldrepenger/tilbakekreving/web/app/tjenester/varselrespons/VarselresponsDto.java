package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.varselrespons;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.BehandlingReferanseAbacAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public class VarselresponsDto implements AbacDto {

    @Valid
    @NotNull
    private BehandlingReferanse behandlingId;

    public Long getBehandlingId() {
        return behandlingId.getBehandlingId();
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return BehandlingReferanseAbacAttributter.fraBehandlingReferanse(behandlingId);
    }
}
