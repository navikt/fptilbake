package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingEvent;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;

public class BehandlingEnhetEvent implements BehandlingEvent {
    private Long fagsakId;
    private Long behandlingId;
    private AktørId aktørId;


    public BehandlingEnhetEvent(Behandling behandling) {
        this.fagsakId = behandling.getFagsakId();
        this.behandlingId = behandling.getId();
        this.aktørId = behandling.getAktørId();
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