package no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste;

import java.util.Collections;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkRepositoryOld;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagOld;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagOldDokumentLink;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;

@ApplicationScoped
public class HistorikkinnslagTjeneste {

    private HistorikkRepositoryOld historikkRepository;

    HistorikkinnslagTjeneste() {
        // for CDI proxy
    }

    @Inject
    public HistorikkinnslagTjeneste(HistorikkRepositoryOld historikkRepository) {
        this.historikkRepository = historikkRepository;
    }

    private void opprettHistorikkinnslag(Long behandlingId,
                                         Long fagsakId,
                                         HistorikkinnslagType historikkinnslagType,
                                         HistorikkAktør historikkAktør,
                                         List<HistorikkinnslagOldDokumentLink> dokumentLinks) {

        HistorikkinnslagOld historikkinnslag = new HistorikkinnslagOld.Builder()
                .medAktør(historikkAktør)
                .medType(historikkinnslagType)
                .medBehandlingId(behandlingId)
                .medFagsakId(fagsakId)
                .medDokumentLinker(dokumentLinks).build();

        settHistorikkinnslagIHverDokumentLink(dokumentLinks, historikkinnslag);

        HistorikkInnslagTekstBuilder builder = new HistorikkInnslagTekstBuilder()
                .medHendelse(historikkinnslagType);
        builder.build(historikkinnslag);

        historikkRepository.lagre(historikkinnslag);
    }

    private void settHistorikkinnslagIHverDokumentLink(List<HistorikkinnslagOldDokumentLink> dokumentLinks, HistorikkinnslagOld historikkinnslag) {
        for (HistorikkinnslagOldDokumentLink dokumentLink : dokumentLinks) {
            dokumentLink.setHistorikkinnslag(historikkinnslag);
        }
    }

    public void opprettHistorikkinnslagForBrevsending(Behandling behandling,
                                                      JournalpostId journalpostId,
                                                      String dokumentId,
                                                      String tittel) {
        HistorikkinnslagOldDokumentLink dokumentLink = new HistorikkinnslagOldDokumentLink();
        dokumentLink.setJournalpostId(journalpostId);
        dokumentLink.setDokumentId(dokumentId);
        dokumentLink.setLinkTekst(tittel);

        opprettHistorikkinnslag(
                behandling.getId(),
            behandling.getFagsakId(),
                HistorikkinnslagType.BREV_SENT,
                HistorikkAktør.VEDTAKSLØSNINGEN,
                Collections.singletonList(dokumentLink));
    }

    public void opprettHistorikkinnslagForBrevBestilt(Behandling behandling, DokumentMalType malType) {
        HistorikkinnslagOld historikkinnslag = new HistorikkinnslagOld.Builder()
                .medAktør(HistorikkAktør.SAKSBEHANDLER)
                .medType(HistorikkinnslagType.BREV_BESTILT)
                .medBehandlingId(behandling.getId())
                .medFagsakId(behandling.getFagsakId()).build();

        HistorikkInnslagTekstBuilder builder = new HistorikkInnslagTekstBuilder()
                .medHendelse(HistorikkinnslagType.BREV_BESTILT)
                .medBegrunnelse(malType.getNavn());
        builder.build(historikkinnslag);

        historikkRepository.lagre(historikkinnslag);
    }

    public void opprettHistorikkinnslagForOpprettetBehandling(Behandling behandling) {
        if (historikkinnslagForBehandlingStartetErLoggetTidligere(behandling.getId(), HistorikkinnslagType.BEH_STARTET)) {
            return;
        }
        HistorikkAktør historikkAktør = behandling.isManueltOpprettet() ? HistorikkAktør.SAKSBEHANDLER : HistorikkAktør.VEDTAKSLØSNINGEN;

        opprettHistorikkinnslag(
                behandling.getId(),
            behandling.getFagsakId(),
                HistorikkinnslagType.TBK_OPPR,
                historikkAktør,
                Collections.emptyList());
    }

    private boolean historikkinnslagForBehandlingStartetErLoggetTidligere(Long behandlingId, HistorikkinnslagType historikkinnslagType) {
        List<HistorikkinnslagOld> eksisterendeHistorikkListe = historikkRepository.hentHistorikk(behandlingId);


        if (!eksisterendeHistorikkListe.isEmpty()) {
            for (HistorikkinnslagOld eksisterendeHistorikk : eksisterendeHistorikkListe) {
                if (historikkinnslagType.equals(eksisterendeHistorikk.getType())) {
                    return true;
                }
            }
        }
        return false;
    }

    public void opprettHistorikkinnslagForHenleggelse(Behandling behandling, HistorikkinnslagType historikkinnslagType, BehandlingResultatType årsak, String begrunnelse, HistorikkAktør aktør) {
        HistorikkInnslagTekstBuilder builder = new HistorikkInnslagTekstBuilder()
                .medHendelse(historikkinnslagType)
                .medÅrsak(årsak)
                .medBegrunnelse(begrunnelse);
        HistorikkinnslagOld historikkinnslag = new HistorikkinnslagOld();
        historikkinnslag.setType(historikkinnslagType);
        historikkinnslag.setBehandling(behandling);
        builder.build(historikkinnslag);

        historikkinnslag.setAktør(aktør);
        historikkRepository.lagre(historikkinnslag);
    }
}
