package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.fp;

import java.util.Optional;
import java.util.UUID;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.pip.PipBehandlingData;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipAktørId;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipBehandlingStatus;

public record FpPipBehandlingInfo(PipAktørId aktørId, String saksnummer, Long behandlingId, UUID behandlingUuid,
                                  PipBehandlingStatus statusForBehandling, String ansvarligSaksbehandler) {

    public FpPipBehandlingInfo(PipBehandlingData data) {
        this(Optional.ofNullable(data.aktørId()).map(AktørId::getId).map(PipAktørId::new).orElse(null),
            data.saksnummer().getVerdi(), data.behandlingId(), data.behandlingUuid(),
            oversettBehandlingStatus(data.behandlingStatus()), data.ansvarligSaksbehandler());
    }

    private static PipBehandlingStatus oversettBehandlingStatus(BehandlingStatus behandlingStatus) {
        return switch (behandlingStatus) {
            case OPPRETTET -> PipBehandlingStatus.OPPRETTET;
            case UTREDES -> PipBehandlingStatus.UTREDES;
            case FATTER_VEDTAK -> PipBehandlingStatus.FATTE_VEDTAK;
            case null, default -> null;
        };
    }

}
