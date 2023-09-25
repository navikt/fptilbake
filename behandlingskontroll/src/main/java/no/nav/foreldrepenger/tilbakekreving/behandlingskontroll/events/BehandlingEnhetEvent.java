package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingEvent;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;

public record BehandlingEnhetEvent(Long fagsakId, Long behandlingId, AktørId aktørId) implements BehandlingEvent {

    public BehandlingEnhetEvent(Behandling behandling) {
        this(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId());
    }

    @Override
    public Long getFagsakId() {
        return fagsakId;
    }

    @Override
    public AktørId getAktørId() {
        return aktørId;
    }

    @Override
    public Long getBehandlingId() {
        return behandlingId;
    }

}
