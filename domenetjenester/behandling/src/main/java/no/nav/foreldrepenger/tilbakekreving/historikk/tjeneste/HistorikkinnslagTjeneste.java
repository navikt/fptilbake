package no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.domene.dokumentarkiv.journal.JournalMetadata;
import no.nav.foreldrepenger.domene.dokumentarkiv.journal.JournalTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.VariantFormat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.personopplysning.NavBrukerKjønn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagDokumentLink;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.arkiv.ArkivFilType;
import no.nav.foreldrepenger.tilbakekreving.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;

@ApplicationScoped
public class HistorikkinnslagTjeneste {

    private static final String VEDLEGG = "Vedlegg";
    private static final String PAPIRSØKNAD = "Papirsøknad";
    private static final String SØKNAD = "Søknad";
    private static final String INNTEKTSMELDING = "Inntektsmelding";
    private static final String ETTERSENDELSE = "Ettersendelse";
    private HistorikkRepository historikkRepository;
    private JournalTjeneste journalTjeneste;
    private PersoninfoAdapter personinfoAdapter;

    HistorikkinnslagTjeneste() {
        // for CDI proxy
    }

    @Inject
    public HistorikkinnslagTjeneste(HistorikkRepository historikkRepository, JournalTjeneste journalTjeneste, PersoninfoAdapter personinfoAdapter) {
        this.historikkRepository = historikkRepository;
        this.journalTjeneste = journalTjeneste;
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
            .medKjoenn(setKjønn(aktørId))
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

    public void opprettHistorikkinnslagForBrevsending(Behandling behandling, JournalpostId journalpostId, String dokumentId, String tittel) {
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

    private NavBrukerKjønn setKjønn(AktørId aktørId) {
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

    void leggTilHistorikkinnslagDokumentlinker(BehandlingType behandlingType, JournalpostId journalpostId, Historikkinnslag historikkinnslag) {
        List<HistorikkinnslagDokumentLink> dokumentLinker = new ArrayList<>();
        if (journalpostId != null) {
            List<JournalMetadata<DokumentTypeId>> journalMetadataListe = journalTjeneste.hentMetadata(journalpostId);

            List<JournalMetadata<DokumentTypeId>> hoveddokumentJournalMetadata = journalMetadataListe.stream().filter(JournalMetadata::getErHoveddokument).collect(Collectors.toList());

            Optional<JournalMetadata<DokumentTypeId>> elektroniskSøknad = hoveddokumentJournalMetadata.stream()
                .filter(it -> VariantFormat.ORIGINAL.equals(it.getVariantFormat())
                    || VariantFormat.FULLVERSJON.equals(it.getVariantFormat())) //Ustrukturerte dokumenter kan ha xml med variantformat SKANNING_META
                .filter(it -> ArkivFilType.XML.equals(it.getArkivFilType())).findFirst();

            leggTilSøknadDokumentLenke(behandlingType, journalpostId, historikkinnslag, dokumentLinker, hoveddokumentJournalMetadata, elektroniskSøknad);
            journalMetadataListe.stream().filter(j -> !j.getErHoveddokument()).forEach(journalMetadata -> dokumentLinker
                .add(lagHistorikkInnslagDokumentLink(journalMetadata, journalpostId, historikkinnslag, VEDLEGG)));
        }

        historikkinnslag.setDokumentLinker(dokumentLinker);
    }

    private void leggTilSøknadDokumentLenke(BehandlingType behandlingType, JournalpostId journalpostId, Historikkinnslag historikkinnslag, List<HistorikkinnslagDokumentLink> dokumentLinker, List<JournalMetadata<DokumentTypeId>> hoveddokumentJournalMetadata, Optional<JournalMetadata<DokumentTypeId>> elektroniskSøknad) {
        if (elektroniskSøknad.isPresent()) {
            final JournalMetadata<DokumentTypeId> journalMetadata = elektroniskSøknad.get();
            String linkTekst = journalMetadata.getDokumentType().equals(DokumentTypeId.INNTEKTSMELDING) ? INNTEKTSMELDING : SØKNAD; // NOSONAR
            dokumentLinker.add(lagHistorikkInnslagDokumentLink(journalMetadata, journalpostId, historikkinnslag, linkTekst));
        } else {
            String linkTekst = BehandlingType.UDEFINERT.equals(behandlingType) ? ETTERSENDELSE : PAPIRSØKNAD;
            Optional<JournalMetadata<DokumentTypeId>> papirSøknad = hoveddokumentJournalMetadata.stream().filter(j -> !ArkivFilType.XML.equals(j.getArkivFilType())).findFirst();
            papirSøknad.ifPresent(journalMetadata -> dokumentLinker.add(lagHistorikkInnslagDokumentLink(journalMetadata, journalpostId, historikkinnslag, linkTekst)));
        }
    }

    private HistorikkinnslagDokumentLink lagHistorikkInnslagDokumentLink(JournalMetadata<DokumentTypeId> journalMetadata, JournalpostId journalpostId, Historikkinnslag historikkinnslag, String linkTekst) {
        HistorikkinnslagDokumentLink historikkinnslagDokumentLink = new HistorikkinnslagDokumentLink();
        historikkinnslagDokumentLink.setDokumentId(journalMetadata.getDokumentId());
        historikkinnslagDokumentLink.setJournalpostId(journalpostId);
        historikkinnslagDokumentLink.setLinkTekst(linkTekst);
        historikkinnslagDokumentLink.setHistorikkinnslag(historikkinnslag);
        return historikkinnslagDokumentLink;
    }

    public void opprettHistorikkinnslagForVedlegg(Long fagsakId, JournalpostId journalpostId, DokumentTypeId dokumentTypeId) {
        Historikkinnslag historikkinnslag = new Historikkinnslag();
        if (dokumentTypeId != null && dokumentTypeId.equals(DokumentTypeId.INNTEKTSMELDING)) {
            historikkinnslag.setAktør(HistorikkAktør.ARBEIDSGIVER);
        } else {
            historikkinnslag.setAktør(HistorikkAktør.SØKER);
        }
        historikkinnslag.setType(HistorikkinnslagType.VEDLEGG_MOTTATT);
        historikkinnslag.setFagsakId(fagsakId);

        leggTilHistorikkinnslagDokumentlinker(BehandlingType.UDEFINERT, journalpostId, historikkinnslag);

        HistorikkInnslagTekstBuilder builder = new HistorikkInnslagTekstBuilder()
            .medHendelse(HistorikkinnslagType.VEDLEGG_MOTTATT);
        builder.build(historikkinnslag);

        historikkRepository.lagre(historikkinnslag);
    }

    public void opprettHistorikkinnslagForVenteFristRelaterteInnslag(Behandling behandling, HistorikkinnslagType historikkinnslagType, LocalDateTime frist, Venteårsak venteårsak) {
        HistorikkInnslagTekstBuilder builder = new HistorikkInnslagTekstBuilder();

        if (frist != null) {
            builder.medHendelse(historikkinnslagType, frist.toLocalDate());
        } else {
            builder.medHendelse(historikkinnslagType);
        }
        if (!Venteårsak.UDEFINERT.equals(venteårsak)) {
            builder.medÅrsak(venteårsak);
        }
        Historikkinnslag historikkinnslag = new Historikkinnslag();
        historikkinnslag.setAktør(HistorikkAktør.VEDTAKSLØSNINGEN);
        historikkinnslag.setType(historikkinnslagType);
        historikkinnslag.setBehandlingId(behandling.getId());
        historikkinnslag.setFagsakId(behandling.getFagsakId());
        builder.build(historikkinnslag);
        historikkRepository.lagre(historikkinnslag);
    }

    public void opprettHistorikkinnslagForBehandlingOppdatertMedNyeOpplysninger(Behandling behandling, BehandlingÅrsakType behandlingÅrsakType) {
        Historikkinnslag historikkinnslag = new Historikkinnslag();
        historikkinnslag.setAktør(HistorikkAktør.VEDTAKSLØSNINGEN);
        historikkinnslag.setType(HistorikkinnslagType.BEH_OPPDATERT_NYE_OPPL);
        historikkinnslag.setBehandlingId(behandling.getId());
        historikkinnslag.setFagsakId(behandling.getFagsakId());

        HistorikkInnslagTekstBuilder builder = new HistorikkInnslagTekstBuilder()
            .medHendelse(HistorikkinnslagType.BEH_OPPDATERT_NYE_OPPL)
            .medBegrunnelse(behandlingÅrsakType);
        builder.build(historikkinnslag);

        historikkRepository.lagre(historikkinnslag);
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
