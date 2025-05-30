package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagRepository;
import no.nav.foreldrepenger.tilbakekreving.historikk.HistorikkTjeneste;

@ApplicationScoped
public class BehandlingHistorikkTjeneste {

    private HistorikkinnslagRepository historikkinnslagRepository;
    private HistorikkTjeneste historikkTjeneste;

    public BehandlingHistorikkTjeneste() {
        // CDI
    }

    @Inject
    public BehandlingHistorikkTjeneste(HistorikkinnslagRepository historikkinnslagRepository, HistorikkTjeneste historikkTjeneste) {
        this.historikkinnslagRepository = historikkinnslagRepository;
        this.historikkTjeneste = historikkTjeneste;
    }

    public void opprettHistorikkinnslagForOpprettetBehandling(Behandling behandling) {
        if (historikkinnslagForBehandlingStartetErLoggetTidligere(behandling.getId())) {
            return;
        }
        var historikkinnslag = new Historikkinnslag.Builder()
            .medBehandlingId(behandling.getId())
            .medFagsakId(behandling.getFagsakId())
            .medAktør(behandling.isManueltOpprettet() ? HistorikkAktør.SAKSBEHANDLER : HistorikkAktør.VEDTAKSLØSNINGEN)
            .medTittel("Tilbakekreving opprettet")
            .build();
        historikkinnslagRepository.lagre(historikkinnslag);
    }

    private boolean historikkinnslagForBehandlingStartetErLoggetTidligere(Long behandlingId) {
        var eksisterendeHistorikkListe = historikkTjeneste.hentForSak(behandlingId);

        if (!eksisterendeHistorikkListe.isEmpty()) {
            for (var eksisterendeHistorikk : eksisterendeHistorikkListe) {
                if ("Tilbakekreving opprettet".equals(eksisterendeHistorikk.tittel())) {
                    return true;
                }
            }
        }
        return false;
    }

}
