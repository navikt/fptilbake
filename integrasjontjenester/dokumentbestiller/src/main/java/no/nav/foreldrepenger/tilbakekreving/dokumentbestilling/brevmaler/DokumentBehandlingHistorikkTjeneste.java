package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.brevmaler;

import jakarta.enterprise.context.ApplicationScoped;

import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagRepository;

@ApplicationScoped
public class DokumentBehandlingHistorikkTjeneste {

    private HistorikkinnslagRepository historikkRepository;

    public DokumentBehandlingHistorikkTjeneste() {
        // CDI
    }

    @Inject
    public DokumentBehandlingHistorikkTjeneste(HistorikkinnslagRepository historikkRepository) {
        this.historikkRepository = historikkRepository;
    }

    protected void opprettHistorikkinnslagForBrevBestilt(DokumentMalType malType, Behandling behandling) {
        var historikinnslag = new Historikkinnslag.Builder()
            .medFagsakId(behandling.getFagsakId())
            .medBehandlingId(behandling.getId())
            .medAktør(HistorikkAktør.SAKSBEHANDLER)
            .medTittel("Brev bestilt")
            .addLinje(malType.getNavn())
            .build();
        historikkRepository.lagre(historikinnslag);
    }
}
