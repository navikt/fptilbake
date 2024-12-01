package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingEvent;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;

public record BehandlingSaksbehandlerEvent(Saksnummer saksnummer, Long fagsakId, Long behandlingId) implements BehandlingEvent {

    public BehandlingSaksbehandlerEvent(Behandling behandling) {
        this(behandling.getSaksnummer(), behandling.getFagsakId(), behandling.getId());
    }

    @Override
    public Long getFagsakId() {
        return fagsakId;
    }

    @Override
    public Saksnummer getSaksnummer() {
        return saksnummer;
    }

    @Override
    public Long getBehandlingId() {
        return behandlingId;
    }

}
