package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf;

import java.util.Objects;

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

    public void opprettHistorikkinnslagBrevSendt(Long behandlingId, JournalpostIdOgDokumentId dokumentreferanse, DetaljertBrevType detaljertBrevType, BrevMottaker brevMottaker, String tittel) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        String historikkinnslagTittel = finnHistorikkinnslagTittel(detaljertBrevType, brevMottaker, tittel);
        opprettHistorikkinnslag(behandling, dokumentreferanse, historikkinnslagTittel);
    }

    private void opprettHistorikkinnslag(Behandling behandling, JournalpostIdOgDokumentId dokumentreferanse, String tittel) {
        historikkinnslagTjeneste.opprettHistorikkinnslagForBrevsending(
            behandling,
            dokumentreferanse.getJournalpostId(),
            dokumentreferanse.getDokumentId(),
            tittel);
    }

    private String finnHistorikkinnslagTittel(DetaljertBrevType detaljertBrevType, BrevMottaker brevMottaker, String tittel) {
        if (detaljertBrevType == DetaljertBrevType.FRITEKST) {
            return Objects.requireNonNull(tittel);
        }
        return switch (brevMottaker) {
            case BRUKER -> finnHistorikkinnslagTittelBrevTilBruker(detaljertBrevType);
            case VERGE -> finnHistorikkinnslagTittelBrevTilVerge(detaljertBrevType);
            default -> throw new IllegalArgumentException("Ikke-støttet mottaker: " + brevMottaker);
        };
    }

    private String finnHistorikkinnslagTittelBrevTilBruker(DetaljertBrevType detaljertBrevType) {
        return switch (detaljertBrevType) {
            case VARSEL -> "Varselbrev Tilbakekreving";
            case KORRIGERT_VARSEL -> "Korrigert Varselbrev Tilbakekreving";
            case VEDTAK -> "Vedtaksbrev Tilbakekreving";
            case HENLEGGELSE -> "Henleggelsesbrev tilbakekreving";
            case INNHENT_DOKUMETASJON -> "Innhent dokumentasjon Tilbakekreving";
            default -> throw new IllegalArgumentException("Ikke-støttet detaljertBrevType: " + detaljertBrevType);
        };
    }

    private String finnHistorikkinnslagTittelBrevTilVerge(DetaljertBrevType detaljertBrevType) {
        return switch (detaljertBrevType) {
            case VARSEL -> "Varselbrev Tilbakekreving til Verge";
            case KORRIGERT_VARSEL -> "Korrigert Varselbrev Tilbakekreving til verge";
            case VEDTAK -> "Vedtaksbrev Tilbakekreving til verge";
            case HENLEGGELSE -> "Henleggelsesbrev tilbakekreving til verge";
            case INNHENT_DOKUMETASJON -> "Innhent dokumentasjon Tilbakekreving til verge";
            default -> throw new IllegalArgumentException("Ikke-støttet detaljertBrevType: " + detaljertBrevType);
        };
    }

}
