package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.DetaljertBrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.JournalpostIdOgDokumentId;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;

@ApplicationScoped
public class HistorikkinnslagBrevTjeneste {

    private BehandlingRepository behandlingRepository;
    private HistorikkinnslagTjeneste historikkinnslagTjeneste;

    public HistorikkinnslagBrevTjeneste() {
        //for CDI proxy
    }

    @Inject
    public HistorikkinnslagBrevTjeneste(HistorikkinnslagTjeneste historikkinnslagTjeneste, BehandlingRepository behandlingRepository) {
        this.historikkinnslagTjeneste = historikkinnslagTjeneste;
        this.behandlingRepository = behandlingRepository;
    }

    public void opprettHistorikkinnslagBrevSendt(Long behandlingId, JournalpostIdOgDokumentId dokumentreferanse, DetaljertBrevType detaljertBrevType, BrevMottaker brevMottaker) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        String tittel = finnHistorikkinnslagTittel(detaljertBrevType, brevMottaker);
        opprettHistorikkinnslag(behandling, dokumentreferanse, tittel);
    }

    private void opprettHistorikkinnslag(Behandling behandling, JournalpostIdOgDokumentId dokumentreferanse, String tittel) {
        historikkinnslagTjeneste.opprettHistorikkinnslagForBrevsending(
            behandling,
            dokumentreferanse.getJournalpostId(),
            dokumentreferanse.getDokumentId(),
            tittel);
    }

    private String finnHistorikkinnslagTittel(DetaljertBrevType detaljertBrevType, BrevMottaker brevMottaker) {
        switch (brevMottaker) {
            case BRUKER:
                return finnHistorikkinnslagTittelBrevTilBruker(detaljertBrevType);
            case VERGE:
                return finnHistorikkinnslagTittelBrevTilVerge(detaljertBrevType);
            default:
                throw new IllegalArgumentException("Ikke-støttet mottaker: " + brevMottaker);
        }
    }

    private String finnHistorikkinnslagTittelBrevTilBruker(DetaljertBrevType detaljertBrevType) {
        switch (detaljertBrevType) {
            case VARSEL:
                return "Varselbrev Tilbakekreving";
            case KORRIGERT_VARSEL:
                return "Korrigert Varselbrev Tilbakekreving";
            case VEDTAK:
                return "Vedtaksbrev Tilbakekreving";
            case HENLEGGELSE:
                return "Henleggelsesbrev tilbakekreving";
            case INNHENT_DOKUMETASJON:
                return "Innhent dokumentasjon Tilbakekreving";
            default:
                throw new IllegalArgumentException("Ikke-støttet detaljertBrevType: " + detaljertBrevType);
        }
    }

    private String finnHistorikkinnslagTittelBrevTilVerge(DetaljertBrevType detaljertBrevType) {
        switch (detaljertBrevType) {
            case VARSEL:
                return "Varselbrev Tilbakekreving til Verge";
            case KORRIGERT_VARSEL:
                return "Korrigert Varselbrev Tilbakekreving til verge";
            case VEDTAK:
                return "Vedtaksbrev Tilbakekreving til verge";
            case HENLEGGELSE:
                return "Henleggelsesbrev tilbakekreving til verge";
            case INNHENT_DOKUMETASJON:
                return "Innhent dokumentasjon Tilbakekreving til verge";
            default:
                throw new IllegalArgumentException("Ikke-støttet detaljertBrevType: " + detaljertBrevType);
        }
    }

}
