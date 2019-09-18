package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingUtil.bestemFristForBehandlingVent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingFeil;
import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BehandlingFeilutbetalingFakta;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.UtbetaltPeriode;
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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fagsak.FagsakTjeneste;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.FpsakKlient;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagAggregate;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.simulering.kontrakt.SimuleringResultatDto;
import no.nav.foreldrepenger.tilbakekreving.simulering.tjeneste.SimuleringIntegrasjonTjeneste;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
@Transaction
public class BehandlingTjenesteImpl implements BehandlingTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(BehandlingTjenesteImpl.class);

    private BehandlingRepository behandlingRepository;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private BehandlingresultatRepository behandlingresultatRepository;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private BehandlingskontrollAsynkTjeneste behandlingskontrollAsynkTjeneste;
    private KravgrunnlagRepository grunnlagRepository;
    private SimuleringIntegrasjonTjeneste simuleringIntegrasjonTjeneste;
    private FagsakTjeneste fagsakTjeneste;
    private HistorikkinnslagTjeneste historikkinnslagTjeneste;
    private FeilutbetalingTjeneste feilutbetalingTjeneste;
    private FpsakKlient fpsakKlient;

    private Period defaultVentefrist;

    BehandlingTjenesteImpl() {
        // CDI
    }

    @Inject
    public BehandlingTjenesteImpl(BehandlingRepositoryProvider behandlingRepositoryProvider, //NOSONAR
                                  BehandlingskontrollProvider behandlingskontrollProvider,
                                  SimuleringIntegrasjonTjeneste simuleringIntegrasjonTjeneste,
                                  FagsakTjeneste fagsakTjeneste,
                                  HistorikkinnslagTjeneste historikkinnslagTjeneste,
                                  FeilutbetalingTjeneste feilutbetalingTjeneste,
                                  FpsakKlient fpsakKlient,
                                  @KonfigVerdi("frist.brukerrespons.varsel") Period defaultVentefrist) {
        this.behandlingskontrollTjeneste = behandlingskontrollProvider.getBehandlingskontrollTjeneste();
        this.behandlingskontrollAsynkTjeneste = behandlingskontrollProvider.getBehandlingskontrollAsynkTjeneste();
        this.simuleringIntegrasjonTjeneste = simuleringIntegrasjonTjeneste;
        this.fagsakTjeneste = fagsakTjeneste;
        this.historikkinnslagTjeneste = historikkinnslagTjeneste;
        this.feilutbetalingTjeneste = feilutbetalingTjeneste;
        this.fpsakKlient = fpsakKlient;
        this.defaultVentefrist = defaultVentefrist;

        this.behandlingRepository = behandlingRepositoryProvider.getBehandlingRepository();
        this.grunnlagRepository = behandlingRepositoryProvider.getGrunnlagRepository();
        this.eksternBehandlingRepository = behandlingRepositoryProvider.getEksternBehandlingRepository();
        this.behandlingresultatRepository = behandlingRepositoryProvider.getBehandlingresultatRepository();
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
    public Long opprettBehandlingManuell(Saksnummer saksnummer, UUID eksternUuid,
                                         FagsakYtelseType fagsakYtelseType, BehandlingType behandlingType) {

        return opprettFørstegangsbehandling(saksnummer, eksternUuid, null, null, fagsakYtelseType, behandlingType);
    }

    @Override
    public Long opprettBehandlingAutomatisk(Saksnummer saksnummer, UUID eksternUuid, long eksternBehandlingId,
                                            AktørId aktørId, FagsakYtelseType fagsakYtelseType,
                                            BehandlingType behandlingType) {
        return opprettFørstegangsbehandling(saksnummer, eksternUuid, eksternBehandlingId, aktørId, fagsakYtelseType, behandlingType);
    }

    @Override
    public void kanEndreBehandling(Long behandlingId, Long versjon) {
        Boolean kanEndreBehandling = behandlingRepository.erVersjonUendret(behandlingId, versjon);
        if (!kanEndreBehandling) {
            throw BehandlingFeil.FACTORY.endringerHarForekommetPåSøknaden().toException();
        }
    }

    @Override
    public Optional<BehandlingFeilutbetalingFakta> hentBehandlingFeilutbetalingFakta(Long behandlingId) {
        Optional<KravgrunnlagAggregate> grunnlagAggregate = grunnlagRepository.finnGrunnlagForBehandlingId(behandlingId);
        EksternBehandling eksternBehandling = eksternBehandlingRepository.hentFraInternId(behandlingId);
        Optional<SimuleringResultatDto> resultat = simuleringIntegrasjonTjeneste.hentResultat(eksternBehandling.getEksternId());
        if (!resultat.isPresent()) {
            throw BehandlingFeil.FACTORY.fantIkkeSimuleringResultatForBehandlingId(behandlingId).toException();
        }
        if (grunnlagAggregate.isPresent() && grunnlagAggregate.get().getGrunnlagØkonomi() != null) {
            // vi skal bare vise posteringer med feilutbetalt periode
            List<KravgrunnlagPeriode432> feilutbetaltPerioder = feilutbetalingTjeneste.finnPerioderMedFeilutbetaltPosteringer(grunnlagAggregate.get().getGrunnlagØkonomi().getPerioder());
            SimuleringResultatDto simuleringResultat = resultat.get();
            BigDecimal aktuellFeilUtbetaltBeløp = BigDecimal.ZERO;
            List<UtbetaltPeriode> utbetaltPerioder = feilutbetalingTjeneste.finnesLogiskPeriode(feilutbetaltPerioder);
            LocalDate totalPeriodeFom = null;
            LocalDate totalPeriodeTom = null;
            for (UtbetaltPeriode utbetaltPeriode : utbetaltPerioder) {
                aktuellFeilUtbetaltBeløp = aktuellFeilUtbetaltBeløp.add(utbetaltPeriode.getBelop());
                feilutbetalingTjeneste.formFeilutbetalingÅrsak(behandlingId, utbetaltPeriode);
                totalPeriodeFom = totalPeriodeFom == null || totalPeriodeFom.isAfter(utbetaltPeriode.getFom()) ? utbetaltPeriode.getFom() : totalPeriodeFom;
                totalPeriodeTom = totalPeriodeTom == null || totalPeriodeTom.isBefore(utbetaltPeriode.getTom()) ? utbetaltPeriode.getTom() : totalPeriodeTom;
            }

            return Optional.of(feilutbetalingTjeneste.lagBehandlingFeilUtbetalingFakta(simuleringResultat, aktuellFeilUtbetaltBeløp, utbetaltPerioder,
                new Periode(totalPeriodeFom, totalPeriodeTom)));
        }
        return Optional.empty();
    }

    @Override
    public boolean erBehandlingHenlagt(Behandling behandling) {
        Optional<Behandlingsresultat> behandlingsresultat = behandlingresultatRepository.hent(behandling);
        return behandlingsresultat.isPresent() && behandlingsresultat.get().erBehandlingHenlagt();
    }

    @Override
    public boolean kanOppretteBehandling(Saksnummer saksnummer, UUID eksternUuid){
        return !(harÅpenBehandling(saksnummer) || harTilbakekrevingAlleredeFinnes(eksternUuid));
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


    private Long opprettFørstegangsbehandling(Saksnummer saksnummer, UUID eksternUuid, Long eksternBehandlingId,
                                              AktørId aktørId, FagsakYtelseType fagsakYtelseType,
                                              BehandlingType behandlingType) {
        logger.info("Oppretter Tilbakekrevingbehandling for [saksnummer: {} ] for ekstern Uuid [ {} ]", saksnummer, eksternUuid);

        validateHarIkkeÅpenTilbakekrevingBehandling(saksnummer,eksternUuid);

        EksternBehandlingsinfoDto eksternBehandlingsinfoDto;
        if (aktørId == null) {
            SamletEksternBehandlingInfo samletEksternBehandlingInfo = hentEksternBehandlingMedAktørId(eksternUuid);
            aktørId = samletEksternBehandlingInfo.getAktørId();
            eksternBehandlingsinfoDto = samletEksternBehandlingInfo.getGrunninformasjon();
        } else {
            eksternBehandlingsinfoDto = hentEksternBehandlingFraFpsak(eksternUuid);
        }
        eksternBehandlingId = hentEksternBehandlingIdHvisFinnesIkke(eksternBehandlingId, eksternBehandlingsinfoDto);

        Fagsak fagsak = fagsakTjeneste.opprettFagsak(saksnummer, aktørId, fagsakYtelseType);

        Behandling behandling = Behandling.nyBehandlingFor(fagsak, behandlingType).build();
        OrganisasjonsEnhet organisasjonsEnhet = new OrganisasjonsEnhet(eksternBehandlingsinfoDto.getBehandlendeEnhetId(), eksternBehandlingsinfoDto.getBehandlendeEnhetNavn());
        behandling.setBehandlendeOrganisasjonsEnhet(organisasjonsEnhet);

        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        Long behandlingId = behandlingRepository.lagre(behandling, lås);

        EksternBehandling eksternBehandling = new EksternBehandling(behandling, eksternBehandlingId, eksternUuid);
        eksternBehandlingRepository.lagre(eksternBehandling);

        historikkinnslagTjeneste.opprettHistorikkinnslagForOpprettetBehandling(behandling); // FIXME: sjekk om journalpostId skal hentes ///

        behandlingskontrollAsynkTjeneste.asynkProsesserBehandling(behandling);

        return behandlingId;
    }


    private void validateHarIkkeÅpenTilbakekrevingBehandling(Saksnummer saksnummer, UUID eksternUuid) {
        if (harÅpenBehandling(saksnummer)) {
            throw BehandlingFeil.FACTORY.kanIkkeOppretteTilbakekrevingBehandling(saksnummer).toException();
        }
        if (harTilbakekrevingAlleredeFinnes(eksternUuid)) {
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

    private boolean harTilbakekrevingAlleredeFinnes(UUID eksternUuid) {
        return eksternBehandlingRepository.finnForSisteAvsluttetTbkBehandling(eksternUuid).isPresent();
    }

    private Long hentEksternBehandlingIdHvisFinnesIkke(Long eksternBehandlingId, EksternBehandlingsinfoDto eksternBehandlingsinfoDto) {
        if (eksternBehandlingId == null) {
            eksternBehandlingId = eksternBehandlingsinfoDto.getId();
        }
        return eksternBehandlingId;
    }

}
