package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.manuelt;

import java.time.Period;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.FaktaFeilutbetalingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BehandlingFeilutbetalingFakta;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.DetaljertBrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
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
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf.PdfBrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.TekstformatererVarselbrev;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.VarselbrevSamletInfo;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.VarselbrevUtil;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.felles.Frister;

@ApplicationScoped
public class ManueltVarselBrevTjeneste {

    private VarselRepository varselRepository;
    private BehandlingRepository behandlingRepository;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private VergeRepository vergeRepository;

    private EksternDataForBrevTjeneste eksternDataForBrevTjeneste;
    private FaktaFeilutbetalingTjeneste faktaFeilutbetalingTjeneste;

    private PdfBrevTjeneste pdfBrevTjeneste;

    ManueltVarselBrevTjeneste() {
        // for CDI
    }

    @Inject
    public ManueltVarselBrevTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                     EksternDataForBrevTjeneste eksternDataForBrevTjeneste,
                                     FaktaFeilutbetalingTjeneste faktaFeilutbetalingTjeneste,
                                     PdfBrevTjeneste pdfBrevTjeneste) {
        this.varselRepository = repositoryProvider.getVarselRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
        this.vergeRepository = repositoryProvider.getVergeRepository();

        this.eksternDataForBrevTjeneste = eksternDataForBrevTjeneste;
        this.faktaFeilutbetalingTjeneste = faktaFeilutbetalingTjeneste;
        this.pdfBrevTjeneste = pdfBrevTjeneste;
    }

    public void sendManueltVarselBrev(Long behandlingId, String fritekst, BrevMottaker brevMottaker) {
        var behandling = behandlingRepository.hentBehandling(behandlingId);
        var varselbrevSamletInfo = lagVarselBeløpForSending(fritekst, behandling, brevMottaker, false);

        var data = lagManueltVarselBrev(varselbrevSamletInfo);

        var varsletFeilutbetaling = varselbrevSamletInfo.getSumFeilutbetaling();
        pdfBrevTjeneste.sendBrev(behandlingId, DetaljertBrevType.VARSEL, varsletFeilutbetaling, fritekst, BrevData.builder()
                .setMottaker(brevMottaker)
                .setMetadata(data.getBrevMetadata())
                .setOverskrift(data.getOverskrift())
                .setBrevtekst(data.getBrevtekst())
                .build());
    }

    public byte[] hentForhåndsvisningManueltVarselbrev(Long behandlingId, DokumentMalType malType, String fritekst) {
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
        var behandling = behandlingRepository.hentBehandling(behandlingId);
        var varselbrevSamletInfo = lagVarselBeløpForSending(fritekst, behandling, brevMottaker, true);
        var varselInfo = varselRepository.finnEksaktVarsel(behandlingId);

        var data = lagKorrigertVarselBrev(varselbrevSamletInfo, varselInfo);

        var varsletFeilutbetaling = varselbrevSamletInfo.getSumFeilutbetaling();
        pdfBrevTjeneste.sendBrev(behandlingId, DetaljertBrevType.KORRIGERT_VARSEL, varsletFeilutbetaling, fritekst, BrevData.builder()
                .setMottaker(brevMottaker)
                .setOverskrift(data.getOverskrift())
                .setBrevtekst(data.getBrevtekst())
                .setMetadata(data.getBrevMetadata())
                .build());
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
        Personinfo personinfo = eksternDataForBrevTjeneste.hentPerson(behandling.getFagsak().getFagsakYtelseType(), behandling.getAktørId().getId());
        Adresseinfo adresseinfo = eksternDataForBrevTjeneste.hentAdresse(behandling.getFagsak().getFagsakYtelseType(), personinfo, brevMottaker, vergeEntitet);
        String vergeNavn = BrevMottakerUtil.getVergeNavn(vergeEntitet, adresseinfo);

        //Henter fagsaktypenavn på riktig språk
        Språkkode mottakersSpråkkode = hentSpråkkode(behandling.getId());
        FagsakYtelseType fagsakYtelseType = behandling.getFagsak().getFagsakYtelseType();
        YtelseNavn ytelseNavn = eksternDataForBrevTjeneste.hentYtelsenavn(fagsakYtelseType, mottakersSpråkkode);

        Period ventetid = Frister.BRUKER_TILSVAR;

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

}
