package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.pip.PipBehandlingData;

public record K9PipBehandlingInfo(Set<K9PipAktørId> aktørId, String saksnummer, K9PipFagsakStatus fagsakstatus,
                                  K9PipBehandlingStatus statusForBehandling, String ansvarligSaksbehandler) {

    public K9PipBehandlingInfo(PipBehandlingData data) {
        this(Optional.ofNullable(data.getAktørId()).orElse(Set.of()).stream().map(AktørId::getId).map(K9PipAktørId::new).collect(Collectors.toSet()),
            data.getSaksnummer(), oversettAbacFagstatus(),
            oversettAbacBehandlingStatus(data.getStatusForBehandling()), data.getAnsvarligSaksbehandler().orElse(null));
    }

    public Set<K9PipAktørId> getAktørIdNonNull() {
        return aktørId() != null ? aktørId() : Set.of();
    }

    private static K9PipFagsakStatus oversettAbacFagstatus() {
        // Hardkodet for å tillate å opprette TBK
        return K9PipFagsakStatus.UNDER_BEHANDLING;
    }

    private static K9PipBehandlingStatus oversettAbacBehandlingStatus(String kode) {
        if (BehandlingStatus.OPPRETTET.getKode().equals(kode)) {
            return K9PipBehandlingStatus.OPPRETTET;
        } else if (BehandlingStatus.UTREDES.getKode().equals(kode)) {
            return K9PipBehandlingStatus.UTREDES;
        } else if (BehandlingStatus.FATTER_VEDTAK.getKode().equals(kode)) {
            return K9PipBehandlingStatus.FATTE_VEDTAK;
        } else {
            return null;
        }
    }

}
