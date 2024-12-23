package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.brevmaler;

import jakarta.enterprise.context.ApplicationScoped;

import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkRepositoryTeamAware;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag2;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;

@ApplicationScoped
public class DokumentBehandlingHistorikkTjeneste {

    private HistorikkRepositoryTeamAware historikkRepositoryTeamAware;

    public DokumentBehandlingHistorikkTjeneste() {
        // CDI
    }

    @Inject
    public DokumentBehandlingHistorikkTjeneste(HistorikkRepositoryTeamAware historikkRepositoryTeamAware) {
        this.historikkRepositoryTeamAware = historikkRepositoryTeamAware;
    }

    protected void opprettHistorikkinnslagForBrevBestilt(DokumentMalType malType, Behandling behandling) {
        var historikinnslag = opprettHistorikkinnslagForBrevBestilt(behandling, malType);
        var historikinnslag2 = opprettHistorikkinnslag2ForBrevBestilt(behandling, malType);
        historikkRepositoryTeamAware.lagre(historikinnslag, historikinnslag2);
    }

    private static Historikkinnslag2 opprettHistorikkinnslag2ForBrevBestilt(Behandling behandling, DokumentMalType malType) {
        return new Historikkinnslag2.Builder()
            .medFagsakId(behandling.getFagsakId())
            .medBehandlingId(behandling.getId())
            .medAktør(HistorikkAktør.SAKSBEHANDLER)
            .medTittel("Brev bestilt")
            .addLinje(malType.getNavn())
            .build();
    }
    private static Historikkinnslag opprettHistorikkinnslagForBrevBestilt(Behandling behandling, DokumentMalType malType) {
        Historikkinnslag historikkinnslag = new Historikkinnslag.Builder()
            .medAktør(HistorikkAktør.SAKSBEHANDLER)
            .medType(HistorikkinnslagType.BREV_BESTILT)
            .medBehandlingId(behandling.getId())
            .medFagsakId(behandling.getFagsakId()).build();

        HistorikkInnslagTekstBuilder builder = new HistorikkInnslagTekstBuilder()
            .medHendelse(HistorikkinnslagType.BREV_BESTILT)
            .medBegrunnelse(malType.getNavn());
        builder.build(historikkinnslag);

        return historikkinnslag;
    }



}
