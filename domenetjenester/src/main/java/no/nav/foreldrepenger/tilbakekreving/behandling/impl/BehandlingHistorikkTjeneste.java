package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import jakarta.enterprise.context.ApplicationScoped;

import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkRepositoryTeamAware;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag2;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.historikkv2.HistorikkV2Tjeneste;

@ApplicationScoped
public class BehandlingHistorikkTjeneste {

    private HistorikkRepositoryTeamAware historikkRepositoryTeamAware;
    private HistorikkV2Tjeneste historikkV2Tjeneste;

    public BehandlingHistorikkTjeneste() {
        // CDI
    }

    @Inject
    public BehandlingHistorikkTjeneste(HistorikkRepositoryTeamAware historikkRepositoryTeamAware, HistorikkV2Tjeneste historikkV2Tjeneste) {
        this.historikkRepositoryTeamAware = historikkRepositoryTeamAware;
        this.historikkV2Tjeneste = historikkV2Tjeneste;
    }

    public void opprettHistorikkinnslagForOpprettetBehandling(Behandling behandling) {
        if (historikkinnslagForBehandlingStartetErLoggetTidligere(behandling.getId())) {
            return;
        }

        var historikkinnslag = lagHistorikkinnslag(behandling);
        var historikkinnsalg2 = lagHistorikkinnslag2(behandling);
        historikkRepositoryTeamAware.lagre(historikkinnslag, historikkinnsalg2);
    }

    private static Historikkinnslag2 lagHistorikkinnslag2(Behandling behandling) {
        return new Historikkinnslag2.Builder()
            .medBehandlingId(behandling.getId())
            .medFagsakId(behandling.getFagsakId())
            .medAktør(behandling.isManueltOpprettet() ? HistorikkAktør.SAKSBEHANDLER : HistorikkAktør.VEDTAKSLØSNINGEN)
            .medTittel("Tilbakekreving opprettet")
            .build();
    }

    private static Historikkinnslag lagHistorikkinnslag(Behandling behandling) {
        Historikkinnslag historikkinnslag = new Historikkinnslag.Builder()
            .medAktør(behandling.isManueltOpprettet() ? HistorikkAktør.SAKSBEHANDLER : HistorikkAktør.VEDTAKSLØSNINGEN)
            .medType(HistorikkinnslagType.TBK_OPPR)
            .medBehandlingId(behandling.getId())
            .medFagsakId(behandling.getFagsakId())
            .build();


        HistorikkInnslagTekstBuilder builder = new HistorikkInnslagTekstBuilder()
            .medHendelse(HistorikkinnslagType.TBK_OPPR);
        builder.build(historikkinnslag);
        return historikkinnslag;
    }

    private boolean historikkinnslagForBehandlingStartetErLoggetTidligere(Long behandlingId) {
        var eksisterendeHistorikkListe = historikkV2Tjeneste.hentForSak(behandlingId, null);

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
