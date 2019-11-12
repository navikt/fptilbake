package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.manuelt;

import java.time.Period;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.FaktaFeilutbetalingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BehandlingFeilutbetalingFakta;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevdataRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VarselbrevSporing;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
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
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;

@ApplicationScoped
public class ManueltVarselBrevTjeneste {

    private BrevdataRepository brevdataRepository;
    private VarselRepository varselRepository;
    private BehandlingRepository behandlingRepository;

    private EksternDataForBrevTjeneste eksternDataForBrevTjeneste;
    private FaktaFeilutbetalingTjeneste faktaFeilutbetalingTjeneste;
    private FritekstbrevTjeneste bestillDokumentTjeneste;
    private HistorikkinnslagTjeneste historikkinnslagTjeneste;

    ManueltVarselBrevTjeneste() {
        // for CDI
    }

    @Inject
    public ManueltVarselBrevTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                     BrevdataRepository brevdataRepository,
                                     EksternDataForBrevTjeneste eksternDataForBrevTjeneste,
                                     FaktaFeilutbetalingTjeneste faktaFeilutbetalingTjeneste,
                                     FritekstbrevTjeneste bestillDokumentTjeneste,
                                     HistorikkinnslagTjeneste historikkinnslagTjeneste) {
        this.brevdataRepository = brevdataRepository;
        this.varselRepository = repositoryProvider.getVarselRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();

        this.eksternDataForBrevTjeneste = eksternDataForBrevTjeneste;
        this.faktaFeilutbetalingTjeneste = faktaFeilutbetalingTjeneste;
        this.bestillDokumentTjeneste = bestillDokumentTjeneste;
        this.historikkinnslagTjeneste = historikkinnslagTjeneste;
    }

    public void sendManueltVarselBrev(Long behandlingId, DokumentMalType malType, String fritekst) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        VarselbrevSamletInfo varselbrevSamletInfo = lagVarselBeløpForSending(fritekst, behandling);

        FritekstbrevData data = lagManueltVarselBrev(varselbrevSamletInfo);

        JournalpostIdOgDokumentId dokumentreferanse = bestillDokumentTjeneste.sendFritekstbrev(data);
        opprettHistorikkinnslag(behandling, malType, dokumentreferanse);
        lagreInfoOmVarselbrev(behandlingId, dokumentreferanse);
        lagreInfoOmVarselSendt(behandlingId, fritekst, varselbrevSamletInfo.getSumFeilutbetaling());
    }

    public byte[] hentForhåndsvisningManueltVarselbrev(Long behandlingId, DokumentMalType malType, String fritekst) {
        if (DokumentMalType.VARSEL_DOK.equals(malType)) {
            Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
            VarselbrevSamletInfo varselbrevSamletInfo = lagVarselBeløpForSending(fritekst, behandling);

            FritekstbrevData data = lagManueltVarselBrev(varselbrevSamletInfo);
            return bestillDokumentTjeneste.hentForhåndsvisningFritekstbrev(data);
        }
        return new byte[0];
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

    private VarselbrevSamletInfo lagVarselBeløpForSending(String fritekst, Behandling behandling) {
        //Henter data fra tps
        String aktørId = behandling.getAktørId().getId();
        Personinfo personinfo = eksternDataForBrevTjeneste.hentPerson(aktørId);
        Adresseinfo adresseinfo = eksternDataForBrevTjeneste.hentAdresse(personinfo, aktørId);

        //Henter fagsaktypenavn på riktig språk
        Språkkode mottakersSpråkkode = behandling.getFagsak().getNavBruker().getSpråkkode();
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
            feilutbetalingFakta);
    }

    private void opprettHistorikkinnslag(Behandling behandling, DokumentMalType malType, JournalpostIdOgDokumentId dokumentreferanse) {
        final String TITTEL_VARSELBREV_HISTORIKKINNSLAG = "Varselbrev Tilbakekreving";
        historikkinnslagTjeneste.opprettHistorikkinnslagForBrevsending(
            behandling,
            dokumentreferanse.getJournalpostId(),
            dokumentreferanse.getDokumentId(),
            TITTEL_VARSELBREV_HISTORIKKINNSLAG);
    }

    private void lagreInfoOmVarselbrev(Long behandlingId, JournalpostIdOgDokumentId dokumentreferanse) {
        VarselbrevSporing varselbrevSporing = new VarselbrevSporing.Builder()
            .medBehandlingId(behandlingId)
            .medDokumentId(dokumentreferanse.getDokumentId())
            .medJournalpostId(dokumentreferanse.getJournalpostId())
            .build();
        brevdataRepository.lagreVarselbrevData(varselbrevSporing);
    }

    private void lagreInfoOmVarselSendt(Long behandlingId, String varseltTekst, Long varseltBeløp) {
        varselRepository.lagre(behandlingId, varseltTekst, varseltBeløp);
    }


}
