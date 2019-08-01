package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;

@ApplicationScoped
public class HenleggBehandlingTjeneste {

    private BehandlingRepository behandlingRepository;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private HistorikkinnslagTjeneste historikkinnslagTjeneste;

    HenleggBehandlingTjeneste() {
        // CDI
    }

    @Inject
    public HenleggBehandlingTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                     BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                                     HistorikkinnslagTjeneste historikkinnslagTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.historikkinnslagTjeneste = historikkinnslagTjeneste;
    }

    public void henleggBehandling(long behandlingId, BehandlingResultatType årsakKode, String begrunnelse) {
        doHenleggBehandling(behandlingId, årsakKode, begrunnelse, false);
    }

    private void doHenleggBehandling(long behandlingId, BehandlingResultatType årsakKode, String begrunnelse, boolean avbrytVentendeAutopunkt) {
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandlingId);
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        if (avbrytVentendeAutopunkt && behandling.isBehandlingPåVent()) {
            behandlingskontrollTjeneste.taBehandlingAvVent(behandling, kontekst);
            behandlingskontrollTjeneste.settAutopunkterTilUtført(kontekst, true);
        } else if (behandling.isBehandlingPåVent()) {
            behandlingskontrollTjeneste.taBehandlingAvVent(behandling, kontekst);
        }
        behandlingskontrollTjeneste.henleggBehandling(kontekst, årsakKode);

        if (årsakErHenlegg(årsakKode)) {
            sendHenleggelsesbrev();
        }
        historikkinnslagTjeneste.opprettHistorikkinnslagForHenleggelse(behandling, HistorikkinnslagType.AVBRUTT_BEH, årsakKode, begrunnelse, HistorikkAktør.SAKSBEHANDLER);
    }

    private boolean årsakErHenlegg(BehandlingResultatType årsakKode) {
        return BehandlingResultatType.HENLAGT_SØKNAD_TRUKKET.equals(årsakKode)
                || BehandlingResultatType.HENLAGT_KLAGE_TRUKKET.equals(årsakKode)
                || BehandlingResultatType.HENLAGT_INNSYN_TRUKKET.equals(årsakKode);
    }

    private void sendHenleggelsesbrev() {
//        TODO: implementer funksjonalitet for å sende henleggelsebrev
    }
}
