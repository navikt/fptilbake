package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.extra.Days;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingFeil;
import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BehandlingFeilutbetalingFakta;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.UtbetaltPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollAsynkTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FeilutbetalingAggregate;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FeilutbetalingPeriodeÅrsak;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fagsak.FagsakTjeneste;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.FeilutbetalingÅrsakDto;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.UnderÅrsakDto;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.FpsakKlient;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagAggregate;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagBelop433;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.simulering.kontrakt.SimuleringResultatDto;
import no.nav.foreldrepenger.tilbakekreving.simulering.tjeneste.SimuleringIntegrasjonTjeneste;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.util.FPDateUtil;

@ApplicationScoped
@Transaction
public class BehandlingTjenesteImpl implements BehandlingTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(BehandlingTjenesteImpl.class);

    private BehandlingRepositoryProvider behandlingRepositoryProvider;
    private BehandlingRepository behandlingRepository;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private BehandlingskontrollAsynkTjeneste behandlingskontrollAsynkTjeneste;
    private KravgrunnlagRepository grunnlagRepository;
    private SimuleringIntegrasjonTjeneste simuleringIntegrasjonTjeneste;
    private FagsakTjeneste fagsakTjeneste;
    private HistorikkinnslagTjeneste historikkinnslagTjeneste;
    private FpsakKlient fpsakKlient;

    private Period defaultVentefrist;

    BehandlingTjenesteImpl() {
        // CDI
    }

    @Inject
    public BehandlingTjenesteImpl(BehandlingRepositoryProvider behandlingRepositoryProvider,
                                  BehandlingskontrollProvider behandlingskontrollProvider,
                                  SimuleringIntegrasjonTjeneste simuleringIntegrasjonTjeneste,
                                  FagsakTjeneste fagsakTjeneste,
                                  HistorikkinnslagTjeneste historikkinnslagTjeneste,
                                  FpsakKlient fpsakKlient,
                                  @KonfigVerdi("frist.brukerrespons.varsel") Period defaultVentefrist) {
        this.behandlingRepositoryProvider = behandlingRepositoryProvider;
        this.behandlingskontrollTjeneste = behandlingskontrollProvider.getBehandlingskontrollTjeneste();
        this.behandlingskontrollAsynkTjeneste = behandlingskontrollProvider.getBehandlingskontrollAsynkTjeneste();
        this.simuleringIntegrasjonTjeneste = simuleringIntegrasjonTjeneste;
        this.fagsakTjeneste = fagsakTjeneste;
        this.historikkinnslagTjeneste = historikkinnslagTjeneste;
        this.fpsakKlient = fpsakKlient;
        this.defaultVentefrist = defaultVentefrist;

        this.behandlingRepository = behandlingRepositoryProvider.getBehandlingRepository();
        this.grunnlagRepository = behandlingRepositoryProvider.getGrunnlagRepository();
        this.eksternBehandlingRepository = behandlingRepositoryProvider.getEksternBehandlingRepository();
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

    private void doSetBehandlingPåVent(Long behandlingId, AksjonspunktDefinisjon apDef, LocalDate frist, Venteårsak venteårsak) {
        LocalDateTime fristTid = bestemFristForBehandlingVent(frist);

        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);

        BehandlingStegType behandlingStegFunnet = behandling.getAksjonspunktMedDefinisjonOptional(apDef)
            .map(Aksjonspunkt::getBehandlingStegFunnet)
            .orElse(null); // Dersom autopunkt ikke allerede er opprettet, så er det ikke tilknyttet steg
        behandlingskontrollTjeneste.settBehandlingPåVent(behandling, apDef, behandlingStegFunnet, fristTid, venteårsak);

    }

    private LocalDateTime bestemFristForBehandlingVent(LocalDate frist) {
        return frist != null
            ? LocalDateTime.of(frist, FPDateUtil.nå().toLocalTime())
            : FPDateUtil.nå().plus(defaultVentefrist);
    }

    @Override
    public Behandling hentBehandling(Long behandlingId) {
        return behandlingRepository.hentBehandling(behandlingId);
    }

    @Override
    public Long opprettBehandlingManuell(Saksnummer saksnummer, long eksternBehandlingId, AktørId aktørId, BehandlingType behandlingType) {
        Long fagsakId = fpsakKlient.hentFagsakId(eksternBehandlingId);

        return opprettFørstegangsbehandling(saksnummer, fagsakId, eksternBehandlingId, aktørId, behandlingType);
    }

    @Override
    public Long opprettBehandlingAutomatisk(Saksnummer saksnummer, long fagsakId, long eksternBehandlingId, AktørId aktørId, BehandlingType behandlingType) {
        return opprettFørstegangsbehandling(saksnummer, fagsakId, eksternBehandlingId, aktørId, behandlingType);
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
            List<KravgrunnlagPeriode432> feilutbetaltPerioder = finnPerioderMedFeilutbetaltPosteringer(grunnlagAggregate.get().getGrunnlagØkonomi().getPerioder());
            SimuleringResultatDto simuleringResultat = resultat.get();
            BigDecimal aktuellFeilUtbetaltBeløp = BigDecimal.ZERO;
            List<UtbetaltPeriode> utbetaltPerioder = finnesLogiskPeriode(feilutbetaltPerioder);
            LocalDate totalPeriodeFom = null;
            LocalDate totalPeriodeTom = null;
            for (UtbetaltPeriode utbetaltPeriode : utbetaltPerioder) {
                aktuellFeilUtbetaltBeløp = aktuellFeilUtbetaltBeløp.add(utbetaltPeriode.getBelop());
                formFeilutbetalingÅrsak(behandlingId, utbetaltPeriode);
                totalPeriodeFom = totalPeriodeFom == null || totalPeriodeFom.isAfter(utbetaltPeriode.getFom()) ? utbetaltPeriode.getFom() : totalPeriodeFom;
                totalPeriodeTom = totalPeriodeTom == null || totalPeriodeTom.isBefore(utbetaltPeriode.getTom()) ? utbetaltPeriode.getTom() : totalPeriodeTom;
            }

            return Optional.of(lagBehandlingFeilUtbetalingFakta(simuleringResultat, aktuellFeilUtbetaltBeløp, utbetaltPerioder,
                new Periode(totalPeriodeFom, totalPeriodeTom)));
        }
        return Optional.empty();
    }

    private void formFeilutbetalingÅrsak(Long behandlingId, UtbetaltPeriode utbetaltPeriode) {
        Optional<FeilutbetalingAggregate> feilutbetalingAggregate = behandlingRepositoryProvider.getFeilutbetalingRepository()
            .finnFeilutbetaling(behandlingId);
        if (feilutbetalingAggregate.isPresent()) {
            Optional<FeilutbetalingPeriodeÅrsak> feilutbetalingPeriodeÅrsak = feilutbetalingAggregate.get()
                .getFeilutbetaling()
                .getFeilutbetaltPerioder()
                .stream()
                .filter(periodeÅrsak -> utbetaltPeriode.tilPeriode().equals(periodeÅrsak.getPeriode()))
                .findFirst();
            utbetaltPeriode.setFeilutbetalingÅrsakDto(mapFra(feilutbetalingPeriodeÅrsak));
        }
    }

    private Long opprettFørstegangsbehandling(Saksnummer saksnummer, long fagsakId, long eksternBehandlingId, AktørId aktørId, BehandlingType behandlingType) {
        logger.info("Oppretter behandling for fagsak [id: {} saksnummer: {} ] for ekstern behandling [ {} ]", fagsakId, saksnummer, eksternBehandlingId);

        Fagsak fagsak = fagsakTjeneste.finnEllerOpprettFagsak(fagsakId, saksnummer, aktørId);

        Behandling behandling = Behandling.nyBehandlingFor(fagsak, behandlingType).build();
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        Long behandlingId = behandlingRepository.lagre(behandling, lås);

        EksternBehandling eksternBehandling = new EksternBehandling(behandling, eksternBehandlingId);
        eksternBehandlingRepository.lagre(eksternBehandling);

        historikkinnslagTjeneste.opprettHistorikkinnslagForOpprettetBehandling(behandling); // FIXME: sjekk om journalpostId skal hentes ///

        behandlingskontrollAsynkTjeneste.asynkProsesserBehandling(behandling);

        return behandlingId;
    }

    // FIXME: flytt hjelpemetoder for Feilutbetalingfakta til egen klasse
    private BehandlingFeilutbetalingFakta lagBehandlingFeilUtbetalingFakta(SimuleringResultatDto simuleringResultat, BigDecimal aktuellFeilUtbetaltBeløp,
                                                                           List<UtbetaltPeriode> utbetaltPerioder,
                                                                           Periode totalPeriode) {
        return BehandlingFeilutbetalingFakta.builder()
            .medPerioder(utbetaltPerioder)
            .medAktuellFeilUtbetaltBeløp(aktuellFeilUtbetaltBeløp)
            .medTidligereVarsletBeløp(new BigDecimal(simuleringResultat.getSumFeilutbetaling()).abs())
            .medTotalPeriodeFom(totalPeriode.getFom())
            .medTotalPeriodeTom(totalPeriode.getTom())
            // FIXME:må hente ekte data fra repository.Dette bør fikse med en annen brukerstorien
            .medDatoForRevurderingsvedtak(LocalDate.of(2019, 1, 4))
            .medDatoForVarselSendt(LocalDate.of(2019, 1, 4))
            // FIXME: Der teksten kommer fra, er ikke ferdiggjort. Hardcoded for nå
            .medÅrsakRevurdering("Endring fra bruker Inntekstmelding Nyeregisteropplysninger")
            .medResultatFeilutbetaling("Innviliget:Endring i beregning og uttak.Feilutbetaling med tilbakekreving")
            .build();
    }

    private FeilutbetalingÅrsakDto mapFra(Optional<FeilutbetalingPeriodeÅrsak> årsak) {
        FeilutbetalingÅrsakDto feilutbetalingÅrsakDto = new FeilutbetalingÅrsakDto();
        if (årsak.isPresent()) {
            FeilutbetalingPeriodeÅrsak feilutbetalingPeriodeÅrsak = årsak.get();
            feilutbetalingÅrsakDto.setÅrsakKode(feilutbetalingPeriodeÅrsak.getÅrsak());
            if (StringUtils.isNotEmpty(feilutbetalingPeriodeÅrsak.getUnderÅrsak()))
                feilutbetalingÅrsakDto.leggTilUnderÅrsaker(new UnderÅrsakDto(null, feilutbetalingPeriodeÅrsak.getUnderÅrsak(),
                    feilutbetalingPeriodeÅrsak.getUnderÅrsakKodeverk()));
        }
        return feilutbetalingÅrsakDto;
    }

    private List<KravgrunnlagPeriode432> finnPerioderMedFeilutbetaltPosteringer(List<KravgrunnlagPeriode432> allePerioder) {
        List<KravgrunnlagPeriode432> feilutbetaltPerioder = new ArrayList<>();
        for (KravgrunnlagPeriode432 kravgrunnlagPeriode432 : allePerioder) {
            List<KravgrunnlagBelop433> posteringer = kravgrunnlagPeriode432.getKravgrunnlagBeloper433().stream()
                .filter(belop433 -> belop433.getKlasseType().equals(KlasseType.FEIL)).collect(Collectors.toList());
            if (!posteringer.isEmpty()) {
                kravgrunnlagPeriode432.setKravgrunnlagBeloper433(posteringer);
                feilutbetaltPerioder.add(kravgrunnlagPeriode432);
            }
        }
        return feilutbetaltPerioder;
    }

    private List<UtbetaltPeriode> finnesLogiskPeriode(List<KravgrunnlagPeriode432> feilutbetaltPerioder) {
        LocalDate førsteDag = null;
        LocalDate sisteDag = null;
        BigDecimal belopPerPeriode = BigDecimal.ZERO;
        feilutbetaltPerioder.sort(Comparator.comparing(KravgrunnlagPeriode432::getFom));
        List<UtbetaltPeriode> beregnetPerioider = new ArrayList<>();
        for (KravgrunnlagPeriode432 kgPeriode : feilutbetaltPerioder) {
            // for første gang
            Periode periode = kgPeriode.getPeriode();
            if (førsteDag == null && sisteDag == null) {
                førsteDag = periode.getFom();
                sisteDag = periode.getTom();
            } else {
                // beregn forskjellen mellom to perioder
                int antallDager = Days.between(sisteDag, periode.getFom()).getAmount();
                // hvis forskjellen er mer enn 1 dager eller siste dag er i helgen
                if (antallDager > 1 && sjekkAvvikHvisSisteDagIHelgen(sisteDag, antallDager)) {
                    // lag ny perioder hvis forskjellen er mer enn 1 dager
                    beregnetPerioider.add(UtbetaltPeriode.lagPeriode(førsteDag, sisteDag, belopPerPeriode));
                    førsteDag = periode.getFom();
                    belopPerPeriode = BigDecimal.ZERO;
                }
                sisteDag = periode.getTom();
            }
            belopPerPeriode = belopPerPeriode.add(beregnBelop(kgPeriode.getKravgrunnlagBeloper433()));
        }
        if (belopPerPeriode != BigDecimal.ZERO) {
            beregnetPerioider.add(UtbetaltPeriode.lagPeriode(førsteDag, sisteDag, belopPerPeriode));
        }
        return beregnetPerioider;
    }

    private BigDecimal beregnBelop(List<KravgrunnlagBelop433> beloper433) {
        BigDecimal belopPerPeriode = BigDecimal.ZERO;
        for (KravgrunnlagBelop433 belop433 : beloper433) {
            belopPerPeriode = belopPerPeriode.add(belop433.getNyBelop());
        }
        return belopPerPeriode;
    }

    private boolean sjekkAvvikHvisSisteDagIHelgen(LocalDate sisteDag, int antallDager) {
        if (antallDager == 3 && sisteDag.getDayOfWeek() == DayOfWeek.FRIDAY) {
            return false;
        }
        return antallDager != 2 || sisteDag.getDayOfWeek() != DayOfWeek.SATURDAY;
    }

}
