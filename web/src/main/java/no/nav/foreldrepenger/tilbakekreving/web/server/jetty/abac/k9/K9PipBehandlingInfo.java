package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9;

import java.util.UUID;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.pip.PipBehandlingData;

public record K9PipBehandlingInfo(AktørId aktørId, String saksnummer, Long behandlingId, UUID behandlingUuid,
                                  K9PipFagsakStatus fagsakstatus, K9PipBehandlingStatus statusForBehandling, String ansvarligSaksbehandler) {

    public K9PipBehandlingInfo(PipBehandlingData data) {
        this(data.aktørId(), data.saksnummer().getVerdi(), data.behandlingId(), data.behandlingUuid(),
            oversettAbacFagstatus(), oversettBehandlingStatus(data.behandlingStatus()), data.ansvarligSaksbehandler());
    }

    private static K9PipFagsakStatus oversettAbacFagstatus() {
        // Hardkodet for å tillate å opprette TBK
        return K9PipFagsakStatus.UNDER_BEHANDLING;
    }

    private static K9PipBehandlingStatus oversettBehandlingStatus(BehandlingStatus behandlingStatus) {
        return switch (behandlingStatus) {
            case OPPRETTET -> K9PipBehandlingStatus.OPPRETTET;
            case UTREDES -> K9PipBehandlingStatus.UTREDES;
            case FATTER_VEDTAK -> K9PipBehandlingStatus.FATTE_VEDTAK;
            case null, default -> null;
        };
    }

}
