package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf;

import java.util.List;
import java.util.Objects;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.DetaljertBrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagDokumentLink;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagRepository;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.JournalpostIdOgDokumentId;

@ApplicationScoped
public class HistorikkinnslagBrevTjeneste {

    private BehandlingRepository behandlingRepository;
    private HistorikkinnslagRepository historikkRepository;

    public HistorikkinnslagBrevTjeneste() {
        //for CDI proxy
    }

    @Inject
    public HistorikkinnslagBrevTjeneste(HistorikkinnslagRepository historikkRepository, BehandlingRepository behandlingRepository) {
        this.historikkRepository = historikkRepository;
        this.behandlingRepository = behandlingRepository;
    }

    public void opprettHistorikkinnslagBrevSendt(Long behandlingId, JournalpostIdOgDokumentId dokumentreferanse, DetaljertBrevType detaljertBrevType, BrevMottaker brevMottaker, String tittel) {
        var behandling = behandlingRepository.hentBehandling(behandlingId);
        var historikkinnslagTittel = finnHistorikkinnslagTittel(detaljertBrevType, brevMottaker, tittel);
        var historikkinnslag = opprettHistorikkinnslag2ForBrevsending(behandling, dokumentreferanse, historikkinnslagTittel);
        historikkRepository.lagre(historikkinnslag);
    }

    private static Historikkinnslag opprettHistorikkinnslag2ForBrevsending(Behandling behandling, JournalpostIdOgDokumentId dokumentreferanse, String tittel) {
        var doklink = new HistorikkinnslagDokumentLink.Builder()
            .medLinkTekst(tittel)
            .medDokumentId(dokumentreferanse.getDokumentId())
            .medJournalpostId(dokumentreferanse.getJournalpostId())
            .build();
        return new Historikkinnslag.Builder()
            .medAktør(HistorikkAktør.VEDTAKSLØSNINGEN)
            .medFagsakId(behandling.getFagsakId())
            .medBehandlingId(behandling.getId())
            .medTittel("Brev er sendt")
            .medDokumenter(List.of(doklink))
            .build();
    }

    private String finnHistorikkinnslagTittel(DetaljertBrevType detaljertBrevType, BrevMottaker brevMottaker, String tittel) {
        if (detaljertBrevType == DetaljertBrevType.FRITEKST) {
            return Objects.requireNonNull(tittel);
        }
        return switch (brevMottaker) {
            case BRUKER -> finnHistorikkinnslagTittelBrevTilBruker(detaljertBrevType);
            case VERGE -> finnHistorikkinnslagTittelBrevTilVerge(detaljertBrevType);
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
