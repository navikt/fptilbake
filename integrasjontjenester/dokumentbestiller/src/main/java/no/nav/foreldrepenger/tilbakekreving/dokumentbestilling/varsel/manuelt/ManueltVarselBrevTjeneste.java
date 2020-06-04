package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.manuelt;

import java.time.Period;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.FaktaFeilutbetalingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BehandlingFeilutbetalingFakta;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporing;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.VergeRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselInfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselRepository;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevAktørIdUtil;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.EksternDataForBrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.YtelseNavn;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.JournalpostIdOgDokumentId;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.TekstformatererVarselbrev;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.VarselbrevSamletInfo;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.VarselbrevUtil;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;

@ApplicationScoped
public class ManueltVarselBrevTjeneste {

    public static final String TITTEL_VARSELBREV_HISTORIKKINNSLAG = "Varselbrev Tilbakekreving";
    public static final String TITTEL_VARSELBREV_HISTORIKKINNSLAG_TIL_VERGE = "Varselbrev Tilbakekreving til verge";
    public static final String TITTEL_KORRIGERT_VARSELBREV_HISTORIKKINNSLAG = "Korrigert Varselbrev Tilbakekreving";
    public static final String TITTEL_KORRIGERT_VARSELBREV_HISTORIKKINNSLAG_TIL_VERGE = "Korrigert Varselbrev Tilbakekreving til verge";

    private VarselRepository varselRepository;
    private BehandlingRepository behandlingRepository;
    private BrevSporingRepository brevSporingRepository;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private VergeRepository vergeRepository;

    private EksternDataForBrevTjeneste eksternDataForBrevTjeneste;
    private FaktaFeilutbetalingTjeneste faktaFeilutbetalingTjeneste;
    private FritekstbrevTjeneste bestillDokumentTjeneste;
    private HistorikkinnslagTjeneste historikkinnslagTjeneste;

    ManueltVarselBrevTjeneste() {
        // for CDI
    }

    @Inject
    public ManueltVarselBrevTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                     EksternDataForBrevTjeneste eksternDataForBrevTjeneste,
                                     FaktaFeilutbetalingTjeneste faktaFeilutbetalingTjeneste,
                                     FritekstbrevTjeneste bestillDokumentTjeneste,
                                     HistorikkinnslagTjeneste historikkinnslagTjeneste
    ) {
        this.varselRepository = repositoryProvider.getVarselRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.brevSporingRepository = repositoryProvider.getBrevSporingRepository();
        this.eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
        this.vergeRepository = repositoryProvider.getVergeRepository();

        this.eksternDataForBrevTjeneste = eksternDataForBrevTjeneste;
        this.faktaFeilutbetalingTjeneste = faktaFeilutbetalingTjeneste;
        this.bestillDokumentTjeneste = bestillDokumentTjeneste;
        this.historikkinnslagTjeneste = historikkinnslagTjeneste;
    }

    public void sendManueltVarselBrev(Long behandlingId, String fritekst, BrevMottaker brevMottaker) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        VarselbrevSamletInfo varselbrevSamletInfo = lagVarselBeløpForSending(fritekst, behandling, false);

        FritekstbrevData data = lagManueltVarselBrev(varselbrevSamletInfo);

        JournalpostIdOgDokumentId dokumentreferanse = bestillDokumentTjeneste.sendFritekstbrev(data);
        String tittel = BrevMottaker.VERGE.equals(brevMottaker) ? TITTEL_VARSELBREV_HISTORIKKINNSLAG_TIL_VERGE : TITTEL_VARSELBREV_HISTORIKKINNSLAG;
        opprettHistorikkinnslag(behandling, dokumentreferanse, tittel);
        lagreVarselData(behandlingId, dokumentreferanse, fritekst, varselbrevSamletInfo.getSumFeilutbetaling());
    }

    public byte[] hentForhåndsvisningManueltVarselbrev(Long behandlingId, DokumentMalType malType, String fritekst) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        byte[] dokument = new byte[0];
        if (DokumentMalType.VARSEL_DOK.equals(malType)) {
            VarselbrevSamletInfo varselbrevSamletInfo = lagVarselBeløpForSending(fritekst, behandling, false);
            FritekstbrevData data = lagManueltVarselBrev(varselbrevSamletInfo);
            dokument = bestillDokumentTjeneste.hentForhåndsvisningFritekstbrev(data);
        } else if (DokumentMalType.KORRIGERT_VARSEL_DOK.equals(malType)) {
            VarselbrevSamletInfo varselbrevSamletInfo = lagVarselBeløpForSending(fritekst, behandling, true);
            VarselInfo varselInfo = varselRepository.finnEksaktVarsel(behandlingId);

            FritekstbrevData data = lagKorrigertVarselBrev(varselbrevSamletInfo, varselInfo);
            dokument = bestillDokumentTjeneste.hentForhåndsvisningFritekstbrev(data);
        }
        return dokument;
    }

    public void sendKorrigertVarselBrev(Long behandlingId, String fritekst, BrevMottaker brevMottaker) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        VarselbrevSamletInfo varselbrevSamletInfo = lagVarselBeløpForSending(fritekst, behandling, true);
        VarselInfo varselInfo = varselRepository.finnEksaktVarsel(behandlingId);

        FritekstbrevData data = lagKorrigertVarselBrev(varselbrevSamletInfo, varselInfo);

        JournalpostIdOgDokumentId dokumentreferanse = bestillDokumentTjeneste.sendFritekstbrev(data);
        String tittel = BrevMottaker.VERGE.equals(brevMottaker) ? TITTEL_KORRIGERT_VARSELBREV_HISTORIKKINNSLAG_TIL_VERGE : TITTEL_KORRIGERT_VARSELBREV_HISTORIKKINNSLAG;
        opprettHistorikkinnslag(behandling, dokumentreferanse, tittel);
        lagreVarselData(behandlingId, dokumentreferanse, fritekst, varselbrevSamletInfo.getSumFeilutbetaling());
    }

    private FritekstbrevData lagManueltVarselBrev(VarselbrevSamletInfo varselbrevSamletInfo) {
        String overskrift = TekstformatererVarselbrev.lagVarselbrevOverskrift(varselbrevSamletInfo.getBrevMetadata());
        String brevtekst = TekstformatererVarselbrev.lagVarselbrevFritekst(varselbrevSamletInfo);
        return new FritekstbrevData.Builder()
            .medOverskrift(overskrift)
            .medBrevtekst(brevtekst)
            .medMetadata(varselbrevSamletInfo.getBrevMetadata())
            .build();
    }

    private FritekstbrevData lagKorrigertVarselBrev(VarselbrevSamletInfo varselbrevSamletInfo, VarselInfo varselInfo) {
        String overskrift = TekstformatererVarselbrev.lagKorrigertVarselbrevOverskrift(varselbrevSamletInfo.getBrevMetadata());
        String brevtekst = TekstformatererVarselbrev.lagKorrigertVarselbrevFritekst(varselbrevSamletInfo, varselInfo);
        return new FritekstbrevData.Builder()
            .medOverskrift(overskrift)
            .medBrevtekst(brevtekst)
            .medMetadata(varselbrevSamletInfo.getBrevMetadata())
            .build();
    }

    private VarselbrevSamletInfo lagVarselBeløpForSending(String fritekst, Behandling behandling, boolean erKorrigert) {
        //sjekker om behandlingen har verge
        Optional<VergeEntitet> vergeEntitet = vergeRepository.finnVergeInformasjon(behandling.getId());
        boolean finnesVerge = vergeEntitet.isPresent();

        //Henter data fra tps
        String aktørId = BrevAktørIdUtil.getMottakerAktørId(behandling,vergeEntitet);
        Personinfo personinfo = eksternDataForBrevTjeneste.hentPerson(aktørId);
        Adresseinfo adresseinfo = eksternDataForBrevTjeneste.hentAdresse(personinfo, vergeEntitet);

        //Henter fagsaktypenavn på riktig språk
        Språkkode mottakersSpråkkode = hentSpråkkode(behandling.getId());
        FagsakYtelseType fagsakYtelseType = behandling.getFagsak().getFagsakYtelseType();
        YtelseNavn ytelseNavn = eksternDataForBrevTjeneste.hentYtelsenavn(fagsakYtelseType, mottakersSpråkkode);

        Period ventetid = eksternDataForBrevTjeneste.getBrukersSvarfrist();

        //Henter feilutbetaling fakta
        BehandlingFeilutbetalingFakta feilutbetalingFakta = faktaFeilutbetalingTjeneste.hentBehandlingFeilutbetalingFakta(behandling.getId());

        return VarselbrevUtil.sammenstillInfoFraFagsystemerForSendingManueltVarselBrev(
            behandling,
            personinfo,
            adresseinfo,
            fagsakYtelseType,
            mottakersSpråkkode,
            ytelseNavn,
            ventetid,
            fritekst,
            feilutbetalingFakta,
            finnesVerge,
            erKorrigert);
    }

    private Språkkode hentSpråkkode(Long behandlingId) {
        UUID fpsakBehandlingUuid = eksternBehandlingRepository.hentFraInternId(behandlingId).getEksternUuid();
        SamletEksternBehandlingInfo samletEksternBehandlingInfo = eksternDataForBrevTjeneste.hentYtelsesbehandlingFraFagsystemet(fpsakBehandlingUuid);
        return samletEksternBehandlingInfo.getGrunninformasjon().getSpråkkodeEllerDefault();
    }

    private void opprettHistorikkinnslag(Behandling behandling, JournalpostIdOgDokumentId dokumentreferanse, String tittel) {
        historikkinnslagTjeneste.opprettHistorikkinnslagForBrevsending(
            behandling,
            dokumentreferanse.getJournalpostId(),
            dokumentreferanse.getDokumentId(),
            tittel);
    }

    private void lagreVarselData(Long behandlingId, JournalpostIdOgDokumentId dokumentreferanse, String varseltTekst, Long varseltBeløp) {
        lagreInfoOmVarselbrev(behandlingId, dokumentreferanse);
        lagreInfoOmVarselSendt(behandlingId, varseltTekst, varseltBeløp);
    }

    private void lagreInfoOmVarselbrev(Long behandlingId, JournalpostIdOgDokumentId dokumentreferanse) {
        BrevSporing brevSporing = new BrevSporing.Builder()
            .medBehandlingId(behandlingId)
            .medDokumentId(dokumentreferanse.getDokumentId())
            .medJournalpostId(dokumentreferanse.getJournalpostId())
            .medBrevType(BrevType.VARSEL_BREV)
            .build();
        brevSporingRepository.lagre(brevSporing);
    }

    private void lagreInfoOmVarselSendt(Long behandlingId, String varseltTekst, Long varseltBeløp) {
        varselRepository.lagre(behandlingId, varseltTekst, varseltBeløp);
    }


}
