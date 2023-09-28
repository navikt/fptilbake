package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.pip.PipBehandlingData;
import no.nav.vedtak.sikkerhet.abac.pipdata.AbacPipDto;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipAktørId;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipBehandlingStatus;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipFagsakStatus;

public record PipBehandlingInfo(Set<PipAktørId> aktørId, String saksnummer, PipFagsakStatus fagsakstatus,
                                PipBehandlingStatus statusForBehandling, String ansvarligSaksbehandler) {

    public PipBehandlingInfo(PipBehandlingData data) {
        this(Optional.ofNullable(data.getAktørId()).orElse(Set.of()).stream().map(AktørId::getId).map(PipAktørId::new).collect(Collectors.toSet()),
            data.getSaksnummer(), oversettAbacFagstatus(),
            oversettAbacBehandlingStatus(data.getStatusForBehandling()), data.getAnsvarligSaksbehandler().orElse(null));
    }

    public PipBehandlingInfo(AbacPipDto data) {
        this(data.aktørIder(), null, oversettPipFagstatus(data.fagsakStatus()), data.behandlingStatus(), null);
    }

    public Set<PipAktørId> getAktørIdNonNull() {
        return aktørId() != null ? aktørId() : Set.of();
    }

    private static PipFagsakStatus oversettAbacFagstatus() {
        // Hardkodet for å tillate å opprette TBK
        return PipFagsakStatus.UNDER_BEHANDLING;
    }

    private static PipFagsakStatus oversettPipFagstatus(PipFagsakStatus status) {
        return status != null ? status : PipFagsakStatus.UNDER_BEHANDLING;
    }

    private static PipBehandlingStatus oversettAbacBehandlingStatus(String kode) {
        if (BehandlingStatus.OPPRETTET.getKode().equals(kode)) {
            return PipBehandlingStatus.OPPRETTET;
        } else if (BehandlingStatus.UTREDES.getKode().equals(kode)) {
            return PipBehandlingStatus.UTREDES;
        } else if (BehandlingStatus.FATTER_VEDTAK.getKode().equals(kode)) {
            return PipBehandlingStatus.FATTE_VEDTAK;
        } else {
            return null;
        }
    }

}
