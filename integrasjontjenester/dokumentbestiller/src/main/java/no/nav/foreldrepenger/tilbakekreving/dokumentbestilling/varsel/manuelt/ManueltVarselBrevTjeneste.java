package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.manuelt;

import java.time.Period;
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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselInfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselRepository;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.EksternDataForBrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.YtelseNavn;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.JournalpostIdOgDokumentId;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.TekstformatererVarselbrev;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.VarselbrevOverskrift;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.VarselbrevSamletInfo;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.VarselbrevUtil;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;

@ApplicationScoped
public class ManueltVarselBrevTjeneste {

    public static final String TITTEL_VARSELBREV_HISTORIKKINNSLAG = "Varselbrev Tilbakekreving";
    public static final String TITTEL_KORRIGERT_VARSELBREV_HISTORIKKINNSLAG = "Korrigert Varselbrev Tilbakekreving";

    private VarselRepository varselRepository;
    private BehandlingRepository behandlingRepository;
    private BrevSporingRepository brevSporingRepository;
    private EksternBehandlingRepository eksternBehandlingRepository;

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

        this.eksternDataForBrevTjeneste = eksternDataForBrevTjeneste;
        this.faktaFeilutbetalingTjeneste = faktaFeilutbetalingTjeneste;
        this.bestillDokumentTjeneste = bestillDokumentTjeneste;
        this.historikkinnslagTjeneste = historikkinnslagTjeneste;
    }

    public void sendManueltVarselBrev(Long behandlingId, DokumentMalType malType, String fritekst) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        VarselbrevSamletInfo varselbrevSamletInfo = lagVarselBeløpForSending(fritekst, behandling, false);

        FritekstbrevData data = lagManueltVarselBrev(varselbrevSamletInfo);

        JournalpostIdOgDokumentId dokumentreferanse = bestillDokumentTjeneste.sendFritekstbrev(data);
        opprettHistorikkinnslag(behandling, malType, dokumentreferanse, TITTEL_VARSELBREV_HISTORIKKINNSLAG);
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

    public void sendKorrigertVarselBrev(Long behandlingId, DokumentMalType malType, String fritekst) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        VarselbrevSamletInfo varselbrevSamletInfo = lagVarselBeløpForSending(fritekst, behandling, true);
        VarselInfo varselInfo = varselRepository.finnEksaktVarsel(behandlingId);

        FritekstbrevData data = lagKorrigertVarselBrev(varselbrevSamletInfo, varselInfo);

        JournalpostIdOgDokumentId dokumentreferanse = bestillDokumentTjeneste.sendFritekstbrev(data);
        opprettHistorikkinnslag(behandling, malType, dokumentreferanse, TITTEL_KORRIGERT_VARSELBREV_HISTORIKKINNSLAG);
        lagreVarselData(behandlingId, dokumentreferanse, fritekst, varselbrevSamletInfo.getSumFeilutbetaling());
    }

    private FritekstbrevData lagManueltVarselBrev(VarselbrevSamletInfo varselbrevSamletInfo) {
        String overskrift = VarselbrevOverskrift.finnOverskriftVarselbrev(varselbrevSamletInfo.getBrevMetadata().getFagsaktypenavnPåSpråk());
        String brevtekst = TekstformatererVarselbrev.lagVarselbrevFritekst(varselbrevSamletInfo);
        return new FritekstbrevData.Builder()
            .medOverskrift(overskrift)
            .medBrevtekst(brevtekst)
            .medMetadata(varselbrevSamletInfo.getBrevMetadata())
            .build();
    }

    private FritekstbrevData lagKorrigertVarselBrev(VarselbrevSamletInfo varselbrevSamletInfo, VarselInfo varselInfo) {
        String overskrift = FagsakYtelseType.ENGANGSTØNAD.equals(varselbrevSamletInfo.getBrevMetadata().getFagsaktype()) ?
            VarselbrevOverskrift.finnOverskriftKorrigertVarselbrevEnngangsstønad(varselbrevSamletInfo.getBrevMetadata().getFagsaktypenavnPåSpråk()) :
            VarselbrevOverskrift.finnOverskriftKorrigertVarselbrev(varselbrevSamletInfo.getBrevMetadata().getFagsaktypenavnPåSpråk());

        String brevtekst = TekstformatererVarselbrev.lagKorrigertVarselbrevFritekst(varselbrevSamletInfo, varselInfo);
        return new FritekstbrevData.Builder()
            .medOverskrift(overskrift)
            .medBrevtekst(brevtekst)
            .medMetadata(varselbrevSamletInfo.getBrevMetadata())
            .build();
    }

    private VarselbrevSamletInfo lagVarselBeløpForSending(String fritekst, Behandling behandling, boolean erKorrigert) {
        //Henter data fra tps
        String aktørId = behandling.getAktørId().getId();
        Personinfo personinfo = eksternDataForBrevTjeneste.hentPerson(aktørId);
        Adresseinfo adresseinfo = eksternDataForBrevTjeneste.hentAdresse(personinfo, aktørId);

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
            erKorrigert);
    }

    private Språkkode hentSpråkkode(Long behandlingId) {
        UUID fpsakBehandlingUuid = eksternBehandlingRepository.hentFraInternId(behandlingId).getEksternUuid();
        SamletEksternBehandlingInfo samletEksternBehandlingInfo = eksternDataForBrevTjeneste.hentBehandlingFpsak(fpsakBehandlingUuid, Tillegsinformasjon.PERSONOPPLYSNINGER, Tillegsinformasjon.SØKNAD);
        return samletEksternBehandlingInfo.getGrunninformasjon().getSpråkkodeEllerDefault();
    }

    private void opprettHistorikkinnslag(Behandling behandling, DokumentMalType malType, JournalpostIdOgDokumentId dokumentreferanse, String tittel) {
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
