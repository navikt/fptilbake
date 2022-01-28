package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel;

import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.DetaljertBrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.VergeRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselInfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselRepository;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.HentForhåndsvisningVarselbrevDto;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottakerUtil;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.EksternDataForBrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.YtelseNavn;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf.BrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf.PdfBrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevData;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.simulering.FeilutbetaltePerioderDto;


@ApplicationScoped
@Transactional
public class VarselbrevTjeneste {

    private BehandlingRepository behandlingRepository;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private VarselRepository varselRepository;
    private VergeRepository vergeRepository;

    private EksternDataForBrevTjeneste eksternDataForBrevTjeneste;
    private BehandlingTjeneste behandlingTjeneste;

    private PdfBrevTjeneste pdfBrevTjeneste;


    @Inject
    public VarselbrevTjeneste(BehandlingRepositoryProvider repositoryProvider,
                              EksternDataForBrevTjeneste eksternDataForBrevTjeneste,
                              BehandlingTjeneste behandlingTjeneste,
                              PdfBrevTjeneste pdfBrevTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
        this.varselRepository = repositoryProvider.getVarselRepository();
        this.vergeRepository = repositoryProvider.getVergeRepository();

        this.eksternDataForBrevTjeneste = eksternDataForBrevTjeneste;
        this.behandlingTjeneste = behandlingTjeneste;
        this.pdfBrevTjeneste = pdfBrevTjeneste;
    }

    public VarselbrevTjeneste() {
    }

    public void sendVarselbrev(Long behandlingId, BrevMottaker brevMottaker) {
        VarselbrevSamletInfo varselbrevSamletInfo = lagVarselbrevForSending(behandlingId, brevMottaker);
        String overskrift = TekstformatererVarselbrev.lagVarselbrevOverskrift(varselbrevSamletInfo.getBrevMetadata());
        String brevtekst = TekstformatererVarselbrev.lagVarselbrevFritekst(varselbrevSamletInfo);
        FritekstbrevData data = new FritekstbrevData.Builder()
                .medOverskrift(overskrift)
                .medBrevtekst(brevtekst)
                .medMetadata(varselbrevSamletInfo.getBrevMetadata())
                .build();
        Long varsletFeilutbetaling = varselbrevSamletInfo.getSumFeilutbetaling();
        String fritekst = varselbrevSamletInfo.getFritekstFraSaksbehandler();
        pdfBrevTjeneste.sendBrev(behandlingId, DetaljertBrevType.VARSEL, varsletFeilutbetaling, fritekst, BrevData.builder()
                .setMottaker(brevMottaker)
                .setMetadata(data.getBrevMetadata())
                .setOverskrift(data.getOverskrift())
                .setBrevtekst(data.getBrevtekst())
                .build());
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
        return pdfBrevTjeneste.genererForhåndsvisning(BrevData.builder()
                .setMottaker(varselbrevSamletInfo.getBrevMetadata().isFinnesVerge() ? BrevMottaker.VERGE : BrevMottaker.BRUKER)
                .setMetadata(data.getBrevMetadata())
                .setOverskrift(data.getOverskrift())
                .setBrevtekst(data.getBrevtekst())
                .build());
    }

    private VarselbrevSamletInfo lagVarselbrevForSending(Long behandlingId, BrevMottaker brevMottaker) {
        Behandling behandling = behandlingTjeneste.hentBehandling(behandlingId);

        Optional<VergeEntitet> vergeEntitet = vergeRepository.finnVergeInformasjon(behandlingId);
        boolean finnesVerge = vergeEntitet.isPresent();

        //Henter data fra fptilbakes eget repo for å finne behandlingsid brukt i fpsak, samt saksnummer
        EksternBehandling eksternBehandling = eksternBehandlingRepository.hentFraInternId(behandlingId);
        Henvisning henvisning = eksternBehandling.getHenvisning();

        //Henter data fra fpsak
        Saksnummer saksnummer = behandling.getFagsak().getSaksnummer();
        SamletEksternBehandlingInfo eksternBehandlingsinfoDto = eksternDataForBrevTjeneste.hentYtelsesbehandlingFraFagsystemet(eksternBehandling.getEksternUuid(), Tillegsinformasjon.PERSONOPPLYSNINGER);
        //Henter data fra tps
        Personinfo personinfo = eksternDataForBrevTjeneste.hentPerson(behandling.getAktørId().getId());
        Adresseinfo adresseinfo = eksternDataForBrevTjeneste.hentAdresse(personinfo, brevMottaker, vergeEntitet);
        String vergeNavn = BrevMottakerUtil.getVergeNavn(vergeEntitet, adresseinfo);

        //Henter fagsaktypenavn på riktig språk
        Språkkode mottakersSpråkkode = eksternBehandlingsinfoDto.getGrunninformasjon().getSpråkkodeEllerDefault();
        FagsakYtelseType fagsakYtelseType = behandling.getFagsak().getFagsakYtelseType();
        YtelseNavn ytelseNavn = eksternDataForBrevTjeneste.hentYtelsenavn(fagsakYtelseType, mottakersSpråkkode);

        //Henter data fra fpoppdrag
        //FIXME k9-tilbake må hente fra k9-oppdrag vha UUID. Bør antagelig løses ved:
        // .. splitte eksternDataForBrevTjeneste i 2 (hvorav 1 del er for å hente fra fagsystemet)
        // .. lag 2 implementasjoner av fagsystemdelen
        // .. hentFeilutbetaltePerioder bør ta inn henvisning (eller intern behandlingId) og konvertere til eksernid/uuid
        FeilutbetaltePerioderDto feilutbetaltePerioderDto = eksternDataForBrevTjeneste.hentFeilutbetaltePerioder(henvisning);

        VarselInfo varselInfo = varselRepository.finnEksaktVarsel(behandlingId);
        String varselTekst = varselInfo.getVarselTekst();

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
                finnesVerge,
                vergeNavn);
    }

    public VarselbrevSamletInfo lagVarselbrevForForhåndsvisning(UUID behandlingUuId, String varseltekst, FagsakYtelseType fagsakYtleseType) {
        SamletEksternBehandlingInfo eksternBehandlingsinfo = eksternDataForBrevTjeneste.hentYtelsesbehandlingFraFagsystemet(behandlingUuId, Tillegsinformasjon.PERSONOPPLYSNINGER, Tillegsinformasjon.FAGSAK);
        EksternBehandlingsinfoDto grunninformasjon = eksternBehandlingsinfo.getGrunninformasjon();
        Optional<EksternBehandling> eksternBehandling = eksternBehandlingRepository.hentFraHenvisning(grunninformasjon.getHenvisning());
        Behandling behandling = eksternBehandling.isPresent() ? behandlingRepository.hentBehandling(eksternBehandling.get().getInternId()) : null;
        Optional<VergeEntitet> vergeEntitet = behandling != null ? vergeRepository.finnVergeInformasjon(behandling.getId()) : Optional.empty();
        boolean finnesVerge = vergeEntitet.isPresent();
        BrevMottaker brevMottaker = finnesVerge ? BrevMottaker.VERGE : BrevMottaker.BRUKER;

        Personinfo personinfo = eksternDataForBrevTjeneste.hentPerson(eksternBehandlingsinfo.getAktørId().getId());
        Adresseinfo adresseinfo = eksternDataForBrevTjeneste.hentAdresse(personinfo, brevMottaker, vergeEntitet);
        String vergeNavn = BrevMottakerUtil.getVergeNavn(vergeEntitet, adresseinfo);

        FeilutbetaltePerioderDto feilutbetaltePerioderDto = eksternDataForBrevTjeneste.hentFeilutbetaltePerioder(grunninformasjon.getHenvisning());
        Språkkode mottakersSpråkkode = grunninformasjon.getSpråkkodeEllerDefault();
        YtelseNavn ytelseNavn = eksternDataForBrevTjeneste.hentYtelsenavn(fagsakYtleseType, mottakersSpråkkode);
        return VarselbrevUtil.sammenstillInfoFraFagsystemerForhåndvisningVarselbrev(
                eksternBehandlingsinfo.getSaksnummer(),
                varseltekst,
                adresseinfo,
                eksternBehandlingsinfo,
                personinfo,
                feilutbetaltePerioderDto,
                eksternDataForBrevTjeneste.getBrukersSvarfrist(),
                fagsakYtleseType,
                ytelseNavn,
                finnesVerge,
                vergeNavn);
    }
}
