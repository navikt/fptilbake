package no.nav.foreldrepenger.tilbakekreving.pip;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;

import java.util.UUID;

public record PipBehandlingData(Long behandlingId, UUID behandlingUuid, Saksnummer saksnummer, AktørId aktørId,
                                BehandlingStatus behandlingStatus, String ansvarligSaksbehandler) {
}
