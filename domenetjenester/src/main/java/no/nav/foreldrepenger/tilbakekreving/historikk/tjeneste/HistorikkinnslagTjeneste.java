package no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste;

import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.personopplysning.NavBrukerKjønn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagDokumentLink;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.foreldrepenger.tilbakekreving.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;

@ApplicationScoped
public class HistorikkinnslagTjeneste {

    private HistorikkRepository historikkRepository;
    private PersoninfoAdapter personinfoAdapter;

    HistorikkinnslagTjeneste() {
        // for CDI proxy
    }

    @Inject
    public HistorikkinnslagTjeneste(HistorikkRepository historikkRepository, PersoninfoAdapter personinfoAdapter) {
        this.historikkRepository = historikkRepository;
        this.personinfoAdapter = personinfoAdapter;
    }

    private void opprettHistorikkinnslag(Long behandlingId,
                                         AktørId aktørId,
                                         Long fagsakId,
                                         HistorikkinnslagType historikkinnslagType,
                                         HistorikkAktør historikkAktør,
                                         List<HistorikkinnslagDokumentLink> dokumentLinks) {

        Historikkinnslag historikkinnslag = new Historikkinnslag.Builder()
            .medAktør(historikkAktør)
            .medKjoenn(setKjønn(aktørId, historikkAktør))
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

    private void settHistorikkinnslagIHverDokumentLink(List<HistorikkinnslagDokumentLink> dokumentLinks, Historikkinnslag historikkinnslag) {
        for (HistorikkinnslagDokumentLink dokumentLink : dokumentLinks) {
            dokumentLink.setHistorikkinnslag(historikkinnslag);
        }
    }

    public void opprettHistorikkinnslagForBrevsending(Behandling behandling,
                                                      JournalpostId journalpostId,
                                                      String dokumentId,
                                                      String tittel) {
        HistorikkinnslagDokumentLink dokumentLink = new HistorikkinnslagDokumentLink();
        dokumentLink.setJournalpostId(journalpostId);
        dokumentLink.setDokumentId(dokumentId);
        dokumentLink.setLinkTekst(tittel);

        opprettHistorikkinnslag(
            behandling.getId(),
            behandling.getAktørId(),
            behandling.getFagsakId(),
            HistorikkinnslagType.BREV_SENT,
            HistorikkAktør.VEDTAKSLØSNINGEN,
            Collections.singletonList(dokumentLink));
    }

    public void opprettHistorikkinnslagForBrevBestilt(Behandling behandling, DokumentMalType malType){
        Historikkinnslag historikkinnslag = new Historikkinnslag.Builder()
            .medAktør(HistorikkAktør.SAKSBEHANDLER)
            .medKjoenn(setKjønn(behandling.getAktørId(), HistorikkAktør.SAKSBEHANDLER))
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
            behandling.getAktørId(),
            behandling.getFagsakId(),
            HistorikkinnslagType.TBK_OPPR,
            historikkAktør,
            Collections.emptyList());
    }

    private NavBrukerKjønn setKjønn(AktørId aktørId, HistorikkAktør historikkAktør) {
        if (!HistorikkAktør.SØKER.equals(historikkAktør))
            return NavBrukerKjønn.UDEFINERT;
        Personinfo personinfo = personinfoAdapter.innhentSaksopplysningerForSøker(aktørId);
        if (personinfo != null) {
            return personinfo.getKjønn();
        }
        return NavBrukerKjønn.UDEFINERT;
    }

    private boolean historikkinnslagForBehandlingStartetErLoggetTidligere(Long behandlingId, HistorikkinnslagType historikkinnslagType) {
        List<Historikkinnslag> eksisterendeHistorikkListe = historikkRepository.hentHistorikk(behandlingId);


        if (!eksisterendeHistorikkListe.isEmpty()) {
            for (Historikkinnslag eksisterendeHistorikk : eksisterendeHistorikkListe) {
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
        Historikkinnslag historikkinnslag = new Historikkinnslag();
        historikkinnslag.setType(historikkinnslagType);
        historikkinnslag.setBehandling(behandling);
        builder.build(historikkinnslag);

        historikkinnslag.setAktør(aktør);
        historikkRepository.lagre(historikkinnslag);
    }
}
