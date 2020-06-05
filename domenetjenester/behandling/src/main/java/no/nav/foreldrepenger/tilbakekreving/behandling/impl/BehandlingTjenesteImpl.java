package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingUtil.bestemFristForBehandlingVent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingFeil;
import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollAsynkTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingresultatRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.VergeRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.KildeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fagsak.FagsakTjeneste;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.FpsakKlient;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.VergeDto;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.util.StringUtils;
import no.nav.vedtak.util.env.Environment;

@ApplicationScoped
@Transactional
public class BehandlingTjenesteImpl implements BehandlingTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(BehandlingTjenesteImpl.class);
    public static final String FINN_KRAVGRUNNLAG_TASK = "kravgrunnlag.finn";

    private BehandlingRepository behandlingRepository;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private BehandlingresultatRepository behandlingresultatRepository;
    private ProsessTaskRepository prosessTaskRepository;
    private BehandlingVedtakRepository behandlingVedtakRepository;
    private VergeRepository vergeRepository;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private BehandlingskontrollAsynkTjeneste behandlingskontrollAsynkTjeneste;
    private FagsakTjeneste fagsakTjeneste;
    private HistorikkinnslagTjeneste historikkinnslagTjeneste;
    private FpsakKlient fpsakKlient;

    private Period defaultVentefrist;

    BehandlingTjenesteImpl() {
        // CDI
    }

    @Inject
    public BehandlingTjenesteImpl(BehandlingRepositoryProvider behandlingRepositoryProvider,
                                  ProsessTaskRepository prosessTaskRepository,
                                  BehandlingskontrollProvider behandlingskontrollProvider,
                                  FagsakTjeneste fagsakTjeneste,
                                  HistorikkinnslagTjeneste historikkinnslagTjeneste,
                                  FpsakKlient fpsakKlient,
                                  @KonfigVerdi("frist.brukerrespons.varsel") Period defaultVentefrist) {
        this.behandlingskontrollTjeneste = behandlingskontrollProvider.getBehandlingskontrollTjeneste();
        this.behandlingskontrollAsynkTjeneste = behandlingskontrollProvider.getBehandlingskontrollAsynkTjeneste();
        this.fagsakTjeneste = fagsakTjeneste;
        this.historikkinnslagTjeneste = historikkinnslagTjeneste;
        this.fpsakKlient = fpsakKlient;
        this.defaultVentefrist = defaultVentefrist;

        this.behandlingRepository = behandlingRepositoryProvider.getBehandlingRepository();
        this.eksternBehandlingRepository = behandlingRepositoryProvider.getEksternBehandlingRepository();
        this.behandlingresultatRepository = behandlingRepositoryProvider.getBehandlingresultatRepository();
        this.behandlingVedtakRepository = behandlingRepositoryProvider.getBehandlingVedtakRepository();
        this.vergeRepository = behandlingRepositoryProvider.getVergeRepository();
        this.prosessTaskRepository = prosessTaskRepository;
    }

    @Override
    public List<Behandling> hentBehandlinger(Saksnummer saksnummer) {
        return behandlingRepository.hentAlleBehandlingerForSaksnummer(saksnummer);
    }

    @Override
    public void settBehandlingPaVent(Long behandlingsId, LocalDate frist, Venteårsak venteårsak) {
        AksjonspunktDefinisjon aksjonspunktDefinisjon = AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING;

        doSetBehandlingPåVent(behandlingsId, aksjonspunktDefinisjon, frist, venteårsak);
    }

    @Override
    public void endreBehandlingPåVent(Long behandlingId, LocalDate frist, Venteårsak venteårsak) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        if (!behandling.isBehandlingPåVent()) {
            throw BehandlingFeil.FACTORY.kanIkkeEndreVentefristForBehandlingIkkePaVent(behandlingId).toException();
        }
        AksjonspunktDefinisjon aksjonspunktDefinisjon = behandling.getBehandlingPåVentAksjonspunktDefinisjon();
        doSetBehandlingPåVent(behandlingId, aksjonspunktDefinisjon, frist, venteårsak);
    }

    @Override
    public Behandling hentBehandling(Long behandlingId) {
        return behandlingRepository.hentBehandling(behandlingId);
    }

    @Override
    public Behandling hentBehandling(UUID behandlingUUId) {
        return behandlingRepository.hentBehandling(behandlingUUId);
    }

    @Override
    public Long opprettBehandlingManuell(Saksnummer saksnummer, UUID eksternUuid,
                                         FagsakYtelseType fagsakYtelseType, BehandlingType behandlingType) {

        Behandling behandling = opprettFørstegangsbehandling(saksnummer, eksternUuid, null, null,
            fagsakYtelseType, behandlingType);
        String gruppe = behandlingskontrollAsynkTjeneste.asynkProsesserBehandling(behandling);
        opprettFinnGrunnlagTask(behandling, gruppe);
        return behandling.getId();
    }

    @Override
    public Long opprettBehandlingAutomatisk(Saksnummer saksnummer, UUID eksternUuid, Henvisning eksternBehandlingId,
                                            AktørId aktørId, FagsakYtelseType fagsakYtelseType,
                                            BehandlingType behandlingType) {
        Behandling behandling = opprettFørstegangsbehandling(saksnummer, eksternUuid, eksternBehandlingId, aktørId, fagsakYtelseType, behandlingType);
        behandlingskontrollAsynkTjeneste.asynkProsesserBehandling(behandling);
        return behandling.getId();
    }

    @Override
    public void kanEndreBehandling(Long behandlingId, Long versjon) {
        Boolean kanEndreBehandling = behandlingRepository.erVersjonUendret(behandlingId, versjon);
        if (!kanEndreBehandling) {
            throw BehandlingFeil.FACTORY.endringerHarForekommetPåSøknaden().toException();
        }
    }

    @Override
    public boolean erBehandlingHenlagt(Behandling behandling) {
        Optional<Behandlingsresultat> behandlingsresultat = behandlingresultatRepository.hent(behandling);
        return behandlingsresultat.isPresent() && behandlingsresultat.get().erBehandlingHenlagt();
    }

    @Override
    public boolean kanOppretteBehandling(Saksnummer saksnummer, UUID eksternUuid) {
        return !(harÅpenBehandling(saksnummer) || finnesTilbakekrevingsbehandlingForYtelsesbehandlingen(eksternUuid));
    }

    @Override
    public void oppdaterBehandlingMedEksternReferanse(Saksnummer saksnummer, Henvisning eksternBehandlingId, UUID eksternUuid) {
        List<Behandling> behandlinger = behandlingRepository.hentAlleBehandlingerForSaksnummer(saksnummer);
        if (behandlinger.isEmpty()) {
            throw BehandlingFeil.FACTORY.fantIngenTilbakekrevingBehandlingForSaksnummer(saksnummer).toException();
        }
        Optional<Behandling> åpenTilbakekrevingBehandling = behandlinger.stream()
            .filter(behandling -> !behandling.erAvsluttet())
            .filter(behandling -> BehandlingType.TILBAKEKREVING.equals(behandling.getType()))
            .findAny();

        if (åpenTilbakekrevingBehandling.isPresent()) {
            Behandling behandling = åpenTilbakekrevingBehandling.get();
            EksternBehandling eksternBehandling = new EksternBehandling(behandling, eksternBehandlingId, eksternUuid);
            eksternBehandlingRepository.lagre(eksternBehandling);
        }
    }

    @Override
    public Optional<BehandlingVedtak> hentBehandlingvedtakForBehandlingId(long behandlingId) {
        return behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(behandlingId);
    }

    private void doSetBehandlingPåVent(Long behandlingId, AksjonspunktDefinisjon apDef, LocalDate frist, Venteårsak venteårsak) {
        LocalDateTime fristTid = bestemFristForBehandlingVent(frist, defaultVentefrist);

        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);

        BehandlingStegType behandlingStegFunnet = behandling.getAksjonspunktMedDefinisjonOptional(apDef)
            .map(Aksjonspunkt::getBehandlingStegFunnet)
            .orElse(null); // Dersom autopunkt ikke allerede er opprettet, så er det ikke tilknyttet steg
        behandlingskontrollTjeneste.settBehandlingPåVent(behandling, apDef, behandlingStegFunnet, fristTid, venteårsak);

    }

    private SamletEksternBehandlingInfo hentEksternBehandlingMedAktørId(UUID eksternUuid) {
        return fpsakKlient.hentBehandlingsinfo(eksternUuid, Tillegsinformasjon.PERSONOPPLYSNINGER);
    }

    private EksternBehandlingsinfoDto hentEksternBehandlingFraFpsak(UUID eksternUuid) {
        Optional<EksternBehandlingsinfoDto> eksternBehandlingsInfo = fpsakKlient.hentBehandling(eksternUuid);
        if (eksternBehandlingsInfo.isPresent()) {
            return eksternBehandlingsInfo.get();
        }
        throw BehandlingFeil.FACTORY.fantIkkeEksternBehandlingForUuid(eksternUuid.toString()).toException();
    }


    private Behandling opprettFørstegangsbehandling(Saksnummer saksnummer, UUID eksternUuid, Henvisning henvisning, AktørId aktørId, FagsakYtelseType fagsakYtelseType, BehandlingType behandlingType) {
        //FIXME k9-tilbake får ikke eksternBehandlingId
        logger.info("Oppretter Tilbakekrevingbehandling for [saksnummer: {} ] for ekstern Uuid [ {} ]", saksnummer, eksternUuid);
        validateHarIkkeÅpenTilbakekrevingBehandling(saksnummer, eksternUuid);
        boolean manueltOpprettet = false;
        EksternBehandlingsinfoDto eksternBehandlingsinfoDto;
        if (aktørId == null) {
            //FIXME trenger en mer intuitiv måte å skille manuell/automatisk opprettelse enn ved at aktørid er satt eller ikke.
            SamletEksternBehandlingInfo samletEksternBehandlingInfo = hentEksternBehandlingMedAktørId(eksternUuid);
            aktørId = samletEksternBehandlingInfo.getAktørId();
            eksternBehandlingsinfoDto = samletEksternBehandlingInfo.getGrunninformasjon();
            manueltOpprettet = true;
        } else {
            eksternBehandlingsinfoDto = hentEksternBehandlingFraFpsak(eksternUuid);
        }
        henvisning = hentHenvisningHvisIkkeFinnes(henvisning, eksternBehandlingsinfoDto);

        Fagsak fagsak = fagsakTjeneste.opprettFagsak(saksnummer, aktørId, fagsakYtelseType);

        Behandling behandling = Behandling.nyBehandlingFor(fagsak, behandlingType)
            .medManueltOpprettet(manueltOpprettet).build();
        OrganisasjonsEnhet organisasjonsEnhet = new OrganisasjonsEnhet(eksternBehandlingsinfoDto.getBehandlendeEnhetId(), eksternBehandlingsinfoDto.getBehandlendeEnhetNavn());
        behandling.setBehandlendeOrganisasjonsEnhet(organisasjonsEnhet);

        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, lås);

        EksternBehandling eksternBehandling = new EksternBehandling(behandling, henvisning, eksternUuid);
        eksternBehandlingRepository.lagre(eksternBehandling);

        historikkinnslagTjeneste.opprettHistorikkinnslagForOpprettetBehandling(behandling); // FIXME: sjekk om journalpostId skal hentes ///

        hentVergeInformasjonFraFpsak(behandling.getId());

        return behandling;
    }


    private void validateHarIkkeÅpenTilbakekrevingBehandling(Saksnummer saksnummer, UUID eksternUuid) {
        if (harÅpenBehandling(saksnummer)) {
            throw BehandlingFeil.FACTORY.kanIkkeOppretteTilbakekrevingBehandling(saksnummer).toException();
        }
        if (finnesTilbakekrevingsbehandlingForYtelsesbehandlingen(eksternUuid)) {
            throw BehandlingFeil.FACTORY.kanIkkeOppretteTilbakekrevingBehandling(eksternUuid).toException();
        }
    }

    private boolean harÅpenBehandling(Saksnummer saksnummer) {
        List<Behandling> behandlinger = hentBehandlinger(saksnummer);
        if (behandlinger.isEmpty()) {
            return false;
        }
        return behandlinger.stream().anyMatch(behandling -> !behandling.erAvsluttet()
            && BehandlingType.TILBAKEKREVING.equals(behandling.getType()));
    }

    private boolean finnesTilbakekrevingsbehandlingForYtelsesbehandlingen(UUID eksternUuid) {
        Optional<EksternBehandling> eksternBehandling = eksternBehandlingRepository.finnForSisteAvsluttetTbkBehandling(eksternUuid);
        if (eksternBehandling.isPresent()) {
            Behandling behandling = behandlingRepository.hentBehandling(eksternBehandling.get().getInternId());
            return !erBehandlingHenlagt(behandling); //hvis behandlingen er henlagt,kan opprettes nye behandling
        }
        return false;
    }

    private Henvisning hentHenvisningHvisIkkeFinnes(Henvisning henvisning, EksternBehandlingsinfoDto eksternBehandlingsinfoDto) {
        if (henvisning == null) {
            henvisning = eksternBehandlingsinfoDto.getHenvisning();
            if (henvisning == null) {
                throw new NullPointerException("Henvisning fra saksbehandlingsklienten var null");
            }
        }
        return henvisning;
    }

    private void opprettFinnGrunnlagTask(Behandling behandling, String fortsettBehandlingProsessTaskGruppe) {
        ProsessTaskData prosessTaskData = new ProsessTaskData(FINN_KRAVGRUNNLAG_TASK);
        prosessTaskData.setGruppe(fortsettBehandlingProsessTaskGruppe);
        prosessTaskData.setSekvens("2");
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskRepository.lagre(prosessTaskData);
    }

    //TODO verge bør flyttes til egen tjeneste
    private void hentVergeInformasjonFraFpsak(long behandlingId) {
        if (erTestMiljø()) {
            EksternBehandling eksternBehandling = eksternBehandlingRepository.hentFraInternId(behandlingId);
            SamletEksternBehandlingInfo eksternBehandlingInfo = fpsakKlient.hentBehandlingsinfo(eksternBehandling.getEksternUuid(), Tillegsinformasjon.VERGE);
            if (eksternBehandlingInfo.getVerge() != null) {
                lagreVergeInformasjon(behandlingId, eksternBehandlingInfo.getVerge());
            }
        }
    }

    //TODO verge bør flyttes til egen tjeneste
    private void lagreVergeInformasjon(long behandlingId, VergeDto vergeDto) {
        if (vergeDto.getGyldigTom().isBefore(LocalDate.now())) {
            logger.info("Verge informasjon er utløpt.Så kopierer ikke fra fpsak");
        } else {
            VergeEntitet.Builder builder = VergeEntitet.builder().medVergeType(vergeDto.getVergeType())
                .medKilde(KildeType.FPSAK.name())
                .medGyldigPeriode(vergeDto.getGyldigFom(), vergeDto.getGyldigTom())
                .medNavn(vergeDto.getNavn())
                .medBegrunnelse("");
            if (!StringUtils.nullOrEmpty(vergeDto.getOrganisasjonsnummer())) {
                builder.medOrganisasjonnummer(vergeDto.getOrganisasjonsnummer());
            } else if (!StringUtils.nullOrEmpty(vergeDto.getFnr())) {
                builder.medVergeAktørId(fagsakTjeneste.hentAktørForFnr(vergeDto.getFnr()));
            }
            vergeRepository.lagreVergeInformasjon(behandlingId, builder.build());
        }
    }

    //midlertidig kode. skal fjernes etter en stund
    private boolean erTestMiljø() {
        //foreløpig kun på for testing
        boolean isEnabled = !Environment.current().isProd();
        logger.info("{} er {}", "Hent vergeInformasjon er", isEnabled ? "skudd på" : "ikke skudd på");
        return isEnabled;
    }

}
