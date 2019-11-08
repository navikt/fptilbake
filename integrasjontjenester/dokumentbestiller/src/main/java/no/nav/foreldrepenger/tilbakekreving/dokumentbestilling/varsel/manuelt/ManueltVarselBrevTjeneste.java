package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.manuelt;

import java.time.Period;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BehandlingFeilutbetalingFakta;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevdataRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VarselbrevSporing;
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

    private EksternDataForBrevTjeneste eksternDataForBrevTjeneste;
    private BehandlingTjeneste behandlingTjeneste;
    private FritekstbrevTjeneste bestillDokumentTjeneste;
    private HistorikkinnslagTjeneste historikkinnslagTjeneste;

    ManueltVarselBrevTjeneste() {
        // for CDI
    }

    @Inject
    public ManueltVarselBrevTjeneste(BrevdataRepository brevdataRepository,
                                     VarselRepository varselRepository,
                                     EksternDataForBrevTjeneste eksternDataForBrevTjeneste,
                                     BehandlingTjeneste behandlingTjeneste,
                                     FritekstbrevTjeneste bestillDokumentTjeneste,
                                     HistorikkinnslagTjeneste historikkinnslagTjeneste) {
        this.brevdataRepository = brevdataRepository;
        this.varselRepository = varselRepository;

        this.eksternDataForBrevTjeneste = eksternDataForBrevTjeneste;
        this.behandlingTjeneste = behandlingTjeneste;
        this.bestillDokumentTjeneste = bestillDokumentTjeneste;
        this.historikkinnslagTjeneste = historikkinnslagTjeneste;
    }

    public void sendManueltVarselBrev(Long behandlingId, DokumentMalType malType, String fritekst) {
        Behandling behandling = behandlingTjeneste.hentBehandling(behandlingId);
        VarselbrevSamletInfo varselbrevSamletInfo = lagVarselBeløpForSending(fritekst, behandling);

        String overskrift = VarselbrevOverskrift.finnOverskriftVarselbrev(varselbrevSamletInfo.getBrevMetadata().getFagsaktypenavnPåSpråk());
        String brevtekst = TekstformatererVarselbrev.lagVarselbrevFritekst(varselbrevSamletInfo);
        FritekstbrevData data = new FritekstbrevData.Builder()
            .medOverskrift(overskrift)
            .medBrevtekst(brevtekst)
            .medMetadata(varselbrevSamletInfo.getBrevMetadata())
            .build();
        JournalpostIdOgDokumentId dokumentreferanse = bestillDokumentTjeneste.sendFritekstbrev(data);
        opprettHistorikkinnslag(behandling, malType, dokumentreferanse);
        lagreInfoOmVarselbrev(behandlingId, dokumentreferanse);
        lagreInfoOmVarselSendt(behandlingId, fritekst, varselbrevSamletInfo.getSumFeilutbetaling());
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
        Optional<BehandlingFeilutbetalingFakta> fakta = behandlingTjeneste.hentBehandlingFeilutbetalingFakta(behandling.getId());
        BehandlingFeilutbetalingFakta feilutbetalingFakta = fakta.get(); //NOSONAR

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
