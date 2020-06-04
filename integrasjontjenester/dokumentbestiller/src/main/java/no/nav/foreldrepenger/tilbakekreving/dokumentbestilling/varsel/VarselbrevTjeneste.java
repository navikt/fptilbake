package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporing;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.VergeRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselInfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselRepository;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.HentForhåndsvisningVarselbrevDto;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevAktørIdUtil;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.EksternDataForBrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.YtelseNavn;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.JournalpostIdOgDokumentId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.simulering.kontrakt.FeilutbetaltePerioderDto;


@ApplicationScoped
@Transactional
public class VarselbrevTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(VarselbrevTjeneste.class);
    private static final String TITTEL_VARSELBREV_HISTORIKKINNSLAG = "Varselbrev Tilbakekreving";
    private static final String TITTEL_VARSELBREV_HISTORIKKINNSLAG_TIL_VERGE = "Varselbrev Tilbakekreving til Verge";

    private BehandlingRepository behandlingRepository;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private BrevSporingRepository brevSporingRepository;
    private VarselRepository varselRepository;
    private VergeRepository vergeRepository;

    private EksternDataForBrevTjeneste eksternDataForBrevTjeneste;
    private BehandlingTjeneste behandlingTjeneste;
    private FritekstbrevTjeneste bestillDokumentTjeneste;
    private HistorikkinnslagTjeneste historikkinnslagTjeneste;


    @Inject
    public VarselbrevTjeneste(BehandlingRepositoryProvider repositoryProvider,
                              EksternDataForBrevTjeneste eksternDataForBrevTjeneste,
                              BehandlingTjeneste behandlingTjeneste,
                              FritekstbrevTjeneste bestillDokumentTjeneste,
                              HistorikkinnslagTjeneste historikkinnslagTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
        this.varselRepository = repositoryProvider.getVarselRepository();
        this.brevSporingRepository = repositoryProvider.getBrevSporingRepository();
        this.vergeRepository = repositoryProvider.getVergeRepository();

        this.eksternDataForBrevTjeneste = eksternDataForBrevTjeneste;
        this.behandlingTjeneste = behandlingTjeneste;
        this.bestillDokumentTjeneste = bestillDokumentTjeneste;
        this.historikkinnslagTjeneste = historikkinnslagTjeneste;
    }

    public VarselbrevTjeneste() {
    }

    public Optional<JournalpostIdOgDokumentId> sendVarselbrev(Long behandlingId, BrevMottaker brevMottaker) {
        VarselbrevSamletInfo varselbrevSamletInfo = lagVarselbrevForSending(behandlingId);
        String overskrift = TekstformatererVarselbrev.lagVarselbrevOverskrift(varselbrevSamletInfo.getBrevMetadata());
        String brevtekst = TekstformatererVarselbrev.lagVarselbrevFritekst(varselbrevSamletInfo);
        FritekstbrevData data = new FritekstbrevData.Builder()
            .medOverskrift(overskrift)
            .medBrevtekst(brevtekst)
            .medMetadata(varselbrevSamletInfo.getBrevMetadata())
            .build();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        JournalpostIdOgDokumentId dokumentreferanse = bestillDokumentTjeneste.sendFritekstbrev(data);
        opprettHistorikkinnslag(behandling, dokumentreferanse, brevMottaker);
        lagreInfoOmVarselbrev(behandlingId, dokumentreferanse);
        return Optional.of(dokumentreferanse);
    }

    public byte[] hentForhåndsvisningVarselbrev(HentForhåndsvisningVarselbrevDto hentForhåndsvisningVarselbrevDto) {
        VarselbrevSamletInfo varselbrevSamletInfo = lagVarselbrevForForhåndsvisning(
            hentForhåndsvisningVarselbrevDto.getBehandlingUuid(),
            hentForhåndsvisningVarselbrevDto.getVarseltekst(),
            hentForhåndsvisningVarselbrevDto.getFagsakYtelseType());

        String overskrift = TekstformatererVarselbrev.lagVarselbrevOverskrift(varselbrevSamletInfo.getBrevMetadata());
        String brevtekst = TekstformatererVarselbrev.lagVarselbrevFritekst(varselbrevSamletInfo);
        FritekstbrevData data = new FritekstbrevData.Builder()
            .medOverskrift(overskrift)
            .medBrevtekst(brevtekst)
            .medMetadata(varselbrevSamletInfo.getBrevMetadata())
            .build();

        return bestillDokumentTjeneste.hentForhåndsvisningFritekstbrev(data);
    }

    private void opprettHistorikkinnslag(Behandling behandling, JournalpostIdOgDokumentId dokumentreferanse, BrevMottaker brevMottaker) {
        String tittel = BrevMottaker.VERGE.equals(brevMottaker) ? TITTEL_VARSELBREV_HISTORIKKINNSLAG_TIL_VERGE : TITTEL_VARSELBREV_HISTORIKKINNSLAG;
        historikkinnslagTjeneste.opprettHistorikkinnslagForBrevsending(
            behandling,
            dokumentreferanse.getJournalpostId(),
            dokumentreferanse.getDokumentId(),
            tittel);
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

    private VarselbrevSamletInfo lagVarselbrevForSending(Long behandlingId) {
        Behandling behandling = behandlingTjeneste.hentBehandling(behandlingId);

        Optional<VergeEntitet> vergeEntitet = vergeRepository.finnVergeInformasjon(behandlingId);
        boolean finnesVerge = vergeEntitet.isPresent();

        //Henter data fra fptilbakes eget repo for å finne behandlingsid brukt i fpsak, samt saksnummer
        EksternBehandling eksternBehandling = eksternBehandlingRepository.hentFraInternId(behandlingId);
        Long behandlingIdIFpsak = eksternBehandling.getEksternId();

        //Henter data fra fpsak
        Saksnummer saksnummer = behandling.getFagsak().getSaksnummer();
        SamletEksternBehandlingInfo eksternBehandlingsinfoDto = eksternDataForBrevTjeneste.hentYtelsesbehandlingFraFagsystemet(eksternBehandling.getEksternUuid(), Tillegsinformasjon.PERSONOPPLYSNINGER);
        //Henter data fra tps
        String aktørId = BrevAktørIdUtil.getMottakerAktørId(behandling,vergeEntitet);
        Personinfo personinfo = eksternDataForBrevTjeneste.hentPerson(aktørId);
        Adresseinfo adresseinfo = eksternDataForBrevTjeneste.hentAdresse(personinfo,vergeEntitet);

        //Henter fagsaktypenavn på riktig språk
        Språkkode mottakersSpråkkode = eksternBehandlingsinfoDto.getGrunninformasjon().getSpråkkodeEllerDefault();
        FagsakYtelseType fagsakYtelseType = behandling.getFagsak().getFagsakYtelseType();
        YtelseNavn ytelseNavn = eksternDataForBrevTjeneste.hentYtelsenavn(fagsakYtelseType, mottakersSpråkkode);

        //Henter data fra fpoppdrag
        //FIXME k9-tilbake må hente fra k9-oppdrag vha UUID. Bør antagelig løses ved:
        // .. splitte eksternDataForBrevTjeneste i 2 (hvorav 1 del er for å hente fra fagsystemet)
        // .. lag 2 implementasjoner av fagsystemdelen
        // .. hentFeilutbetaltePerioder bør ta inn henvisning (eller intern behandlingId) og konvertere til eksernid/uuid
        FeilutbetaltePerioderDto feilutbetaltePerioderDto = eksternDataForBrevTjeneste.hentFeilutbetaltePerioder(behandlingIdIFpsak);

        VarselInfo varselInfo = varselRepository.finnEksaktVarsel(behandlingId);
        String varselTekst = varselInfo.getVarselTekst();
        lagreVarseltBeløp(behandlingId, feilutbetaltePerioderDto.getSumFeilutbetaling());

        return VarselbrevUtil.sammenstillInfoFraFagsystemerForSending(
            eksternBehandlingsinfoDto,
            saksnummer,
            adresseinfo,
            personinfo,
            feilutbetaltePerioderDto,
            eksternDataForBrevTjeneste.getBrukersSvarfrist(),
            fagsakYtelseType,
            ytelseNavn,
            varselTekst,
            finnesVerge);
    }

    public VarselbrevSamletInfo lagVarselbrevForForhåndsvisning(UUID behandlingUuId, String varseltekst, FagsakYtelseType fagsakYtleseType) {
        SamletEksternBehandlingInfo eksternBehandlingsinfo = eksternDataForBrevTjeneste.hentYtelsesbehandlingFraFagsystemet(behandlingUuId, Tillegsinformasjon.PERSONOPPLYSNINGER, Tillegsinformasjon.FAGSAK);
        EksternBehandlingsinfoDto grunninformasjon = eksternBehandlingsinfo.getGrunninformasjon();
        Optional<EksternBehandling> eksternBehandling = eksternBehandlingRepository.hentFraEksternId(grunninformasjon.getId());
        Behandling behandling = eksternBehandling.isPresent() ? behandlingRepository.hentBehandling(eksternBehandling.get().getInternId()) : null;
        Optional<VergeEntitet> vergeEntitet = behandling != null ? vergeRepository.finnVergeInformasjon(behandling.getId()) : Optional.empty();
        boolean finnesVerge = vergeEntitet.isPresent();

        String aktørId = finnesVerge ? BrevAktørIdUtil.getMottakerAktørId(behandling,vergeEntitet) : eksternBehandlingsinfo.getAktørId().getId();
        Personinfo personinfo = eksternDataForBrevTjeneste.hentPerson(aktørId);
        Adresseinfo adresseinfo = eksternDataForBrevTjeneste.hentAdresse(personinfo, vergeEntitet);
        FeilutbetaltePerioderDto feilutbetaltePerioderDto = eksternDataForBrevTjeneste.hentFeilutbetaltePerioder(grunninformasjon.getId());
        Språkkode mottakersSpråkkode = grunninformasjon.getSpråkkodeEllerDefault();
        YtelseNavn ytelseNavn = eksternDataForBrevTjeneste.hentYtelsenavn(fagsakYtleseType, mottakersSpråkkode);
        return VarselbrevUtil.sammenstillInfoFraFagsystemerForhåndvisningVarselbrev(
            eksternBehandlingsinfo.getSaksnummer(),
            varseltekst,
            adresseinfo,
            eksternBehandlingsinfo,
            feilutbetaltePerioderDto,
            eksternDataForBrevTjeneste.getBrukersSvarfrist(),
            fagsakYtleseType,
            ytelseNavn,
            finnesVerge);
    }

    private void lagreVarseltBeløp(Long behandlingId, Long varseltBeløp) {
        varselRepository.lagreVarseltBeløp(behandlingId, varseltBeløp);
    }
}
