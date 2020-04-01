package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll;

import java.time.LocalDateTime;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingEvent;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;

public class BehandlingFristenUtløptEvent implements BehandlingEvent {
    private Long fagsakId;
    private Long behandlingId;
    private AktørId aktørId;
    private LocalDateTime fristDato;

    public BehandlingFristenUtløptEvent(Behandling behandling, LocalDateTime fristDato){
        this.fagsakId = behandling.getFagsakId();
        this.behandlingId = behandling.getId();
        this.aktørId = behandling.getAktørId();
        this.fristDato = fristDato;
    }

    @Override
    public Long getBehandlingId() {
        return behandlingId;
    }

    @Override
    public Long getFagsakId() {
        return fagsakId;
    }

    @Override
    public AktørId getAktørId() {
        return aktørId;
    }

    public LocalDateTime getFristDato() {
        return fristDato;
    }
}
