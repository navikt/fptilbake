package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl;


import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;

public interface OppdaterFagsakStatus {
    void oppdaterFagsakNårBehandlingEndret(Behandling behandling);
}
