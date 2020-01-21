package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.henleggelse;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporing;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselInfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselRepository;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.EksternDataForBrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.YtelseNavn;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.BrevMetadata;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.JournalpostIdOgDokumentId;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;

@ApplicationScoped
public class HenleggelsesbrevTjeneste {

    private static final String OVERSKRIFT_HENLEGGELSESBREV = "NAV har avsluttet saken din om tilbakebetaling";
    private static final String TITTEL_HENLEGGELSESBREV_HISTORIKKINNSLAG = "Henleggelsesbrev tilbakekreving";
    private static final String TITTEL_HENLEGGELSESBREV = "Informasjon om at tilbakekrevingssaken er henlagt";

    private VarselRepository varselRepository;
    private BehandlingRepository behandlingRepository;
    private BrevSporingRepository brevSporingRepository;

    private EksternDataForBrevTjeneste eksternDataForBrevTjeneste;
    private FritekstbrevTjeneste bestillDokumentTjeneste;
    private HistorikkinnslagTjeneste historikkinnslagTjeneste;


    HenleggelsesbrevTjeneste() {
        // for CDI
    }

    @Inject
    public HenleggelsesbrevTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                    EksternDataForBrevTjeneste eksternDataForBrevTjeneste,
                                    FritekstbrevTjeneste bestillDokumentTjenest,
                                    HistorikkinnslagTjeneste historikkinnslagTjeneste) {
        this.varselRepository = repositoryProvider.getVarselRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.brevSporingRepository = repositoryProvider.getBrevSporingRepository();

        this.eksternDataForBrevTjeneste = eksternDataForBrevTjeneste;
        this.bestillDokumentTjeneste = bestillDokumentTjenest;
        this.historikkinnslagTjeneste = historikkinnslagTjeneste;
    }

    public Optional<JournalpostIdOgDokumentId> sendHenleggelsebrev(Long behandlingId) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        HenleggelsesbrevSamletInfo henleggelsesbrevSamletInfo = lagHenleggelsebrevForSending(behandling);
        FritekstbrevData fritekstbrevData = lagHenleggelsebrev(henleggelsesbrevSamletInfo);
        JournalpostIdOgDokumentId dokumentreferanse = bestillDokumentTjeneste.sendFritekstbrev(fritekstbrevData);
        opprettHistorikkinnslag(behandling,dokumentreferanse);
        lagreInfoOmHenleggelsesbrev(behandlingId, dokumentreferanse);
        return Optional.ofNullable(dokumentreferanse);
    }

    public byte[] hentForhåndsvisningHenleggelsebrev(Long behandlingId) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        HenleggelsesbrevSamletInfo henleggelsesbrevSamletInfo = lagHenleggelsebrevForSending(behandling);
        FritekstbrevData fritekstbrevData = lagHenleggelsebrev(henleggelsesbrevSamletInfo);
        return bestillDokumentTjeneste.hentForhåndsvisningFritekstbrev(fritekstbrevData);
    }

    private HenleggelsesbrevSamletInfo lagHenleggelsebrevForSending(Behandling behandling) {
        VarselInfo varselInfo = varselRepository.finnEksaktVarsel(behandling.getId());
        FagsakYtelseType fagsakYtelseType = behandling.getFagsak().getFagsakYtelseType();
        Språkkode språkkode = behandling.getFagsak().getNavBruker().getSpråkkode();

        //Henter data fra tps
        String aktørId = behandling.getAktørId().getId();
        YtelseNavn ytelseNavn = eksternDataForBrevTjeneste.hentYtelsenavn(fagsakYtelseType, språkkode);
        Personinfo personinfo = eksternDataForBrevTjeneste.hentPerson(aktørId);
        Adresseinfo adresseinfo = eksternDataForBrevTjeneste.hentAdresse(personinfo, aktørId);

        BrevMetadata metadata = new BrevMetadata.Builder()
            .medBehandlendeEnhetId(behandling.getBehandlendeEnhetId())
            .medBehandlendeEnhetNavn(behandling.getBehandlendeEnhetNavn())
            .medFagsaktypenavnPåSpråk(ytelseNavn.getNavnPåBrukersSpråk())
            .medFagsaktype(behandling.getFagsak().getFagsakYtelseType())
            .medSprakkode(behandling.getFagsak().getNavBruker().getSpråkkode())
            .medAnsvarligSaksbehandler("VL")
            .medSakspartId(personinfo.getPersonIdent().getIdent())
            .medMottakerAdresse(adresseinfo)
            .medSaksnummer(behandling.getFagsak().getSaksnummer().getVerdi())
            .medSakspartNavn(personinfo.getNavn())
            .medTittel(TITTEL_HENLEGGELSESBREV)
            .build();

        HenleggelsesbrevSamletInfo henleggelsesbrevSamletInfo = new HenleggelsesbrevSamletInfo();
        henleggelsesbrevSamletInfo.setBrevMetadata(metadata);
        henleggelsesbrevSamletInfo.setVarsletDato(varselInfo.getOpprettetTidspunkt().toLocalDate());
        return henleggelsesbrevSamletInfo;
    }

    private FritekstbrevData lagHenleggelsebrev(HenleggelsesbrevSamletInfo henleggelsesbrevSamletInfo) {
        return new FritekstbrevData.Builder()
            .medOverskrift(OVERSKRIFT_HENLEGGELSESBREV)
            .medMetadata(henleggelsesbrevSamletInfo.getBrevMetadata())
            .medBrevtekst(TekstformatererHenleggelsesbrev.lagHenleggelsebrevFritekst(henleggelsesbrevSamletInfo)).build();
    }

    private void opprettHistorikkinnslag(Behandling behandling, JournalpostIdOgDokumentId dokumentreferanse) {
        historikkinnslagTjeneste.opprettHistorikkinnslagForBrevsending(
            behandling,
            dokumentreferanse.getJournalpostId(),
            dokumentreferanse.getDokumentId(),
            TITTEL_HENLEGGELSESBREV_HISTORIKKINNSLAG);
    }

    private void lagreInfoOmHenleggelsesbrev(Long behandlingId, JournalpostIdOgDokumentId journalpostIdOgDokumentId) {
        BrevSporing henleggelsesBrevsporing = new BrevSporing.Builder()
            .medBehandlingId(behandlingId)
            .medJournalpostId(journalpostIdOgDokumentId.getJournalpostId())
            .medDokumentId(journalpostIdOgDokumentId.getDokumentId())
            .medBrevType(BrevType.HENLEGGELSE_BREV).build();

        brevSporingRepository.lagre(henleggelsesBrevsporing);
    }

}
