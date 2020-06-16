package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.henleggelse;

import java.util.Optional;
import java.util.UUID;

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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.VergeRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottakerUtil;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.EksternDataForBrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.YtelseNavn;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.BrevMetadata;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.JournalpostIdOgDokumentId;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;

@ApplicationScoped
public class HenleggelsesbrevTjeneste {

    public static final String TITTEL_HENLEGGELSESBREV_HISTORIKKINNSLAG = "Henleggelsesbrev tilbakekreving";
    public static final String TITTEL_HENLEGGELSESBREV_HISTORIKKINNSLAG_TIL_VERGE = "Henleggelsesbrev tilbakekreving til verge";
    private static final String TITTEL_HENLEGGELSESBREV = "Informasjon om at tilbakekrevingssaken er henlagt";

    private BehandlingRepository behandlingRepository;
    private BrevSporingRepository brevSporingRepository;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private VergeRepository vergeRepository;

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
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.brevSporingRepository = repositoryProvider.getBrevSporingRepository();
        this.eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
        this.vergeRepository = repositoryProvider.getVergeRepository();

        this.eksternDataForBrevTjeneste = eksternDataForBrevTjeneste;
        this.bestillDokumentTjeneste = bestillDokumentTjenest;
        this.historikkinnslagTjeneste = historikkinnslagTjeneste;
    }

    public Optional<JournalpostIdOgDokumentId> sendHenleggelsebrev(Long behandlingId, BrevMottaker brevMottaker) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        HenleggelsesbrevSamletInfo henleggelsesbrevSamletInfo = lagHenleggelsebrevForSending(behandling,brevMottaker);
        FritekstbrevData fritekstbrevData = lagHenleggelsebrev(henleggelsesbrevSamletInfo);
        JournalpostIdOgDokumentId dokumentreferanse = bestillDokumentTjeneste.sendFritekstbrev(fritekstbrevData);
        opprettHistorikkinnslag(behandling, dokumentreferanse, brevMottaker);
        lagreInfoOmHenleggelsesbrev(behandlingId, dokumentreferanse);
        return Optional.ofNullable(dokumentreferanse);
    }

    public byte[] hentForhåndsvisningHenleggelsebrev(Long behandlingId) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        boolean finnesVerge = vergeRepository.finnesVerge(behandlingId);
        BrevMottaker brevMottaker = finnesVerge ? BrevMottaker.VERGE : BrevMottaker.BRUKER;
        HenleggelsesbrevSamletInfo henleggelsesbrevSamletInfo = lagHenleggelsebrevForSending(behandling, brevMottaker);
        FritekstbrevData fritekstbrevData = lagHenleggelsebrev(henleggelsesbrevSamletInfo);
        return bestillDokumentTjeneste.hentForhåndsvisningFritekstbrev(fritekstbrevData);
    }

    private HenleggelsesbrevSamletInfo lagHenleggelsebrevForSending(Behandling behandling, BrevMottaker brevMottaker) {
        Long behandlingId = behandling.getId();
        Optional<BrevSporing> brevSporing = brevSporingRepository.hentSistSendtVarselbrev(behandlingId);
        if (brevSporing.isEmpty()) {
            throw HenleggelsesbrevFeil.FACTORY.kanIkkeSendeEllerForhåndsviseHenleggelsesBrev(behandlingId).toException();
        }
        FagsakYtelseType fagsakYtelseType = behandling.getFagsak().getFagsakYtelseType();
        Språkkode språkkode = hentSpråkkode(behandlingId);
        Optional<VergeEntitet> vergeEntitet = vergeRepository.finnVergeInformasjon(behandlingId);
        boolean finnesVerge = vergeEntitet.isPresent();

        //Henter data fra tps
        YtelseNavn ytelseNavn = eksternDataForBrevTjeneste.hentYtelsenavn(fagsakYtelseType, språkkode);
        Personinfo personinfo = eksternDataForBrevTjeneste.hentPerson(behandling.getAktørId().getId());
        Adresseinfo adresseinfo = eksternDataForBrevTjeneste.hentAdresse(personinfo, brevMottaker, vergeEntitet);
        String vergeNavn = BrevMottakerUtil.getVergeNavn(vergeEntitet,adresseinfo);

        BrevMetadata metadata = new BrevMetadata.Builder()
            .medBehandlendeEnhetId(behandling.getBehandlendeEnhetId())
            .medBehandlendeEnhetNavn(behandling.getBehandlendeEnhetNavn())
            .medFagsaktypenavnPåSpråk(ytelseNavn.getNavnPåBrukersSpråk())
            .medFagsaktype(behandling.getFagsak().getFagsakYtelseType())
            .medSprakkode(språkkode)
            .medAnsvarligSaksbehandler("VL")
            .medSakspartId(personinfo.getPersonIdent().getIdent())
            .medMottakerAdresse(adresseinfo)
            .medSaksnummer(behandling.getFagsak().getSaksnummer().getVerdi())
            .medSakspartNavn(personinfo.getNavn())
            .medVergeNavn(vergeNavn)
            .medFinnesVerge(finnesVerge)
            .medTittel(TITTEL_HENLEGGELSESBREV)
            .build();

        HenleggelsesbrevSamletInfo henleggelsesbrevSamletInfo = new HenleggelsesbrevSamletInfo();
        henleggelsesbrevSamletInfo.setBrevMetadata(metadata);
        henleggelsesbrevSamletInfo.setVarsletDato(brevSporing.get().getOpprettetTidspunkt().toLocalDate());
        return henleggelsesbrevSamletInfo;
    }

    private Språkkode hentSpråkkode(Long behandlingId) {
        UUID fpsakBehandlingUuid = eksternBehandlingRepository.hentForSisteAktivertInternId(behandlingId).getEksternUuid();
        SamletEksternBehandlingInfo samletEksternBehandlingInfo = eksternDataForBrevTjeneste.hentYtelsesbehandlingFraFagsystemet(fpsakBehandlingUuid);
        return samletEksternBehandlingInfo.getGrunninformasjon().getSpråkkodeEllerDefault();
    }

    private FritekstbrevData lagHenleggelsebrev(HenleggelsesbrevSamletInfo henleggelsesbrevSamletInfo) {
        return new FritekstbrevData.Builder()
            .medOverskrift(TekstformatererHenleggelsesbrev.lagHenleggelsebrevOverskrift(henleggelsesbrevSamletInfo))
            .medMetadata(henleggelsesbrevSamletInfo.getBrevMetadata())
            .medBrevtekst(TekstformatererHenleggelsesbrev.lagHenleggelsebrevFritekst(henleggelsesbrevSamletInfo))
            .build();
    }

    private void opprettHistorikkinnslag(Behandling behandling, JournalpostIdOgDokumentId dokumentreferanse, BrevMottaker brevMottaker) {
        String tittel = BrevMottaker.VERGE.equals(brevMottaker) ? TITTEL_HENLEGGELSESBREV_HISTORIKKINNSLAG_TIL_VERGE
            : TITTEL_HENLEGGELSESBREV_HISTORIKKINNSLAG;
        historikkinnslagTjeneste.opprettHistorikkinnslagForBrevsending(
            behandling,
            dokumentreferanse.getJournalpostId(),
            dokumentreferanse.getDokumentId(),
            tittel);
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
