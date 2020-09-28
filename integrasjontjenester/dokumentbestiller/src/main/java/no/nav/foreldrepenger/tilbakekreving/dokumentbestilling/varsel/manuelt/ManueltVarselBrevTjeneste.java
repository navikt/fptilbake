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
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottakerUtil;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.EksternDataForBrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.YtelseNavn;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf.BrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf.BrevToggle;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf.PdfBrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.JournalpostIdOgDokumentId;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.TekstformatererVarselbrev;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.VarselbrevSamletInfo;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.VarselbrevUtil;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.SamletEksternBehandlingInfo;
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

    private PdfBrevTjeneste pdfBrevTjeneste;

    ManueltVarselBrevTjeneste() {
        // for CDI
    }

    @Inject
    public ManueltVarselBrevTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                     EksternDataForBrevTjeneste eksternDataForBrevTjeneste,
                                     FaktaFeilutbetalingTjeneste faktaFeilutbetalingTjeneste,
                                     FritekstbrevTjeneste bestillDokumentTjeneste,
                                     HistorikkinnslagTjeneste historikkinnslagTjeneste,
                                     PdfBrevTjeneste pdfBrevTjeneste) {
        this.varselRepository = repositoryProvider.getVarselRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.brevSporingRepository = repositoryProvider.getBrevSporingRepository();
        this.eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
        this.vergeRepository = repositoryProvider.getVergeRepository();

        this.eksternDataForBrevTjeneste = eksternDataForBrevTjeneste;
        this.faktaFeilutbetalingTjeneste = faktaFeilutbetalingTjeneste;
        this.bestillDokumentTjeneste = bestillDokumentTjeneste;
        this.historikkinnslagTjeneste = historikkinnslagTjeneste;
        this.pdfBrevTjeneste = pdfBrevTjeneste;
    }

    public void sendManueltVarselBrev(Long behandlingId, String fritekst, BrevMottaker brevMottaker) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        VarselbrevSamletInfo varselbrevSamletInfo = lagVarselBeløpForSending(fritekst, behandling, brevMottaker, false);

        FritekstbrevData data = lagManueltVarselBrev(varselbrevSamletInfo);

        JournalpostIdOgDokumentId dokumentreferanse;
        if (BrevToggle.brukDokprod()) {
            dokumentreferanse = bestillDokumentTjeneste.sendFritekstbrev(data);
        } else {
            dokumentreferanse = pdfBrevTjeneste.sendBrevSomIkkeErVedtaksbrev(behandlingId, BrevData.builder()
                .setMottaker(brevMottaker)
                .setMetadata(data.getBrevMetadata())
                .setOverskrift(data.getOverskrift())
                .setBrevtekst(data.getBrevtekst())
                .build());
        }
        String tittel = BrevMottaker.VERGE.equals(brevMottaker) ? TITTEL_VARSELBREV_HISTORIKKINNSLAG_TIL_VERGE : TITTEL_VARSELBREV_HISTORIKKINNSLAG;
        opprettHistorikkinnslag(behandling, dokumentreferanse, tittel);
        lagreVarselData(behandlingId, dokumentreferanse, fritekst, varselbrevSamletInfo.getSumFeilutbetaling());
    }

    public byte[] hentForhåndsvisningManueltVarselbrev(Long behandlingId, DokumentMalType malType, String fritekst) {
        if (BrevToggle.brukDokprod()) {
            return hentForhåndsvisningManueltVarselbrevDokprod(behandlingId, malType, fritekst);
        } else {
            return hentForhåndsvisningManueltVarselbrevPdfgen(behandlingId, malType, fritekst);
        }
    }

    public byte[] hentForhåndsvisningManueltVarselbrevDokprod(Long behandlingId, DokumentMalType malType, String fritekst) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        boolean finnesVerge = vergeRepository.finnesVerge(behandlingId);
        BrevMottaker brevMottaker = finnesVerge ? BrevMottaker.VERGE : BrevMottaker.BRUKER;
        if (DokumentMalType.VARSEL_DOK.equals(malType)) {
            VarselbrevSamletInfo varselbrevSamletInfo = lagVarselBeløpForSending(fritekst, behandling, brevMottaker, false);
            FritekstbrevData data = lagManueltVarselBrev(varselbrevSamletInfo);
            return bestillDokumentTjeneste.hentForhåndsvisningFritekstbrev(data);
        } else if (DokumentMalType.KORRIGERT_VARSEL_DOK.equals(malType)) {
            VarselbrevSamletInfo varselbrevSamletInfo = lagVarselBeløpForSending(fritekst, behandling, brevMottaker, true);
            VarselInfo varselInfo = varselRepository.finnEksaktVarsel(behandlingId);

            FritekstbrevData data = lagKorrigertVarselBrev(varselbrevSamletInfo, varselInfo);
            return bestillDokumentTjeneste.hentForhåndsvisningFritekstbrev(data);
        }
        return new byte[0]; //kaste exception istedet?
    }

    public byte[] hentForhåndsvisningManueltVarselbrevPdfgen(Long behandlingId, DokumentMalType malType, String fritekst) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        boolean finnesVerge = vergeRepository.finnesVerge(behandlingId);
        BrevMottaker brevMottaker = finnesVerge ? BrevMottaker.VERGE : BrevMottaker.BRUKER;
        FritekstbrevData data;
        if (DokumentMalType.VARSEL_DOK.equals(malType)) {
            VarselbrevSamletInfo varselbrevSamletInfo = lagVarselBeløpForSending(fritekst, behandling, brevMottaker, false);
            data = lagManueltVarselBrev(varselbrevSamletInfo);
        } else if (DokumentMalType.KORRIGERT_VARSEL_DOK.equals(malType)) {
            VarselbrevSamletInfo varselbrevSamletInfo = lagVarselBeløpForSending(fritekst, behandling, brevMottaker, true);
            VarselInfo varselInfo = varselRepository.finnEksaktVarsel(behandlingId);
            data = lagKorrigertVarselBrev(varselbrevSamletInfo, varselInfo);
        } else {
            throw new IllegalArgumentException("Ikke-støttet DokumentMalType: " + malType);
        }
        return pdfBrevTjeneste.genererForhåndsvisning(BrevData.builder()
            .setMottaker(brevMottaker)
            .setOverskrift(data.getOverskrift())
            .setBrevtekst(data.getBrevtekst())
            .setMetadata(data.getBrevMetadata())
            .build());
    }

    public void sendKorrigertVarselBrev(Long behandlingId, String fritekst, BrevMottaker brevMottaker) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        VarselbrevSamletInfo varselbrevSamletInfo = lagVarselBeløpForSending(fritekst, behandling, brevMottaker, true);
        VarselInfo varselInfo = varselRepository.finnEksaktVarsel(behandlingId);

        FritekstbrevData data = lagKorrigertVarselBrev(varselbrevSamletInfo, varselInfo);

        JournalpostIdOgDokumentId dokumentreferanse;
        if (BrevToggle.brukDokprod()) {
            dokumentreferanse = bestillDokumentTjeneste.sendFritekstbrev(data);
        } else {
            dokumentreferanse = pdfBrevTjeneste.sendBrevSomIkkeErVedtaksbrev(behandlingId, BrevData.builder()
                .setMottaker(brevMottaker)
                .setOverskrift(data.getOverskrift())
                .setBrevtekst(data.getBrevtekst())
                .setMetadata(data.getBrevMetadata())
                .build());
        }
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

    private VarselbrevSamletInfo lagVarselBeløpForSending(String fritekst, Behandling behandling, BrevMottaker brevMottaker, boolean erKorrigert) {
        //sjekker om behandlingen har verge
        Optional<VergeEntitet> vergeEntitet = vergeRepository.finnVergeInformasjon(behandling.getId());
        boolean finnesVerge = vergeEntitet.isPresent();

        //Henter data fra tps
        Personinfo personinfo = eksternDataForBrevTjeneste.hentPerson(behandling.getAktørId().getId());
        Adresseinfo adresseinfo = eksternDataForBrevTjeneste.hentAdresse(personinfo, brevMottaker, vergeEntitet);
        String vergeNavn = BrevMottakerUtil.getVergeNavn(vergeEntitet, adresseinfo);

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
            vergeNavn,
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
