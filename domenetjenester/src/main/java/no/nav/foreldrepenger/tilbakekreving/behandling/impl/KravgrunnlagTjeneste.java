package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType.FAKTA_FEILUTBETALING;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkRepositoryTeamAware;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag2;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.modell.LogiskPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagBelop433;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.SlettGrunnlagEventPubliserer;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;

@ApplicationScoped
public class KravgrunnlagTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(KravgrunnlagTjeneste.class);
    public static final String BEGRUNNELSE_BEHANDLING_STARTET_FORFRA = "Tilbakekreving startes forfra på grunn av endring i feilutbetalt beløp og/eller perioder";

    private KravgrunnlagRepository kravgrunnlagRepository;
    private BehandlingRepository behandlingRepository;
    private HistorikkRepositoryTeamAware historikkRepository;
    private GjenopptaBehandlingMedGrunnlagTjeneste gjenopptaBehandlingTjeneste;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private AutomatiskSaksbehandlingVurderingTjeneste halvtRettsgebyrTjeneste;
    private SlettGrunnlagEventPubliserer kravgrunnlagEventPubliserer;


    KravgrunnlagTjeneste() {
        // For CDI
    }

    @Inject
    public KravgrunnlagTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                GjenopptaBehandlingMedGrunnlagTjeneste gjenopptaBehandlingTjeneste,
                                BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                                SlettGrunnlagEventPubliserer slettGrunnlagEventPubliserer,
                                AutomatiskSaksbehandlingVurderingTjeneste halvtRettsgebyrTjeneste) {
        this.kravgrunnlagRepository = repositoryProvider.getGrunnlagRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.historikkRepository = repositoryProvider.getHistorikkRepositoryTeamAware();
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.gjenopptaBehandlingTjeneste = gjenopptaBehandlingTjeneste;
        this.halvtRettsgebyrTjeneste = halvtRettsgebyrTjeneste;
        this.kravgrunnlagEventPubliserer = slettGrunnlagEventPubliserer;
    }


    public List<LogiskPeriode> utledLogiskPeriode(Long behandlingId) {
        return LogiskPeriodeTjeneste.utledLogiskPeriode(finnFeilutbetalingPrPeriode(behandlingId));
    }

    private SortedMap<Periode, BigDecimal> finnFeilutbetalingPrPeriode(Long behandlingId) {
        var kravgrunnlag = kravgrunnlagRepository.finnKravgrunnlag(behandlingId);
        return mapFeilutbetalingPrPeriode(kravgrunnlag);
    }

    private SortedMap<Periode, BigDecimal> mapFeilutbetalingPrPeriode(Kravgrunnlag431 kravgrunnlag) {
        SortedMap<Periode, BigDecimal> feilutbetalingPrPeriode = new TreeMap<>(Periode.COMPARATOR);
        for (var kravgrunnlagPeriode432 : kravgrunnlag.getPerioder()) {
            var feilutbetalt = kravgrunnlagPeriode432.getKravgrunnlagBeloper433().stream()
                .filter(beløp433 -> beløp433.getKlasseType() == KlasseType.FEIL)
                .map(KravgrunnlagBelop433::getNyBelop)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);

            if (feilutbetalt.compareTo(BigDecimal.ZERO) != 0) {
                feilutbetalingPrPeriode.put(kravgrunnlagPeriode432.getPeriode(), feilutbetalt);
            }
        }
        return feilutbetalingPrPeriode;
    }

    public Optional<PeriodeMedBeløp> finnTotaltForKravgrunnlag(Long behandlingId) {
        var kravgrunnlagOpt = kravgrunnlagRepository.finnKravgrunnlagOpt(behandlingId);
        if (kravgrunnlagOpt.isPresent()) {
            var periodisert = mapFeilutbetalingPrPeriode(kravgrunnlagOpt.get());
            var fom = periodisert.keySet().stream().map(Periode::getFom).min(Comparator.naturalOrder()).orElse(null);
            var tom = periodisert.keySet().stream().map(Periode::getTom).max(Comparator.naturalOrder()).orElse(null);
            var periode = periodisert.isEmpty() ? null : new Periode(fom, tom);
            var sum = periodisert.values().stream().reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
            return Optional.of(new PeriodeMedBeløp(periode, sum));
        }
        return Optional.empty();
    }


    public void lagreTilbakekrevingsgrunnlagFraØkonomi(Long behandlingId, Kravgrunnlag431 kravgrunnlag431, boolean kravgrunnlagetErGyldig) {
        var behandling = behandlingRepository.hentBehandling(behandlingId);
        if (KravStatusKode.ENDRET.equals(kravgrunnlag431.getKravStatusKode())) {
            //TODO KravgrunnlagTjeneste bør ikke være ansvarlig for å bytte steg/sette på vent. Bør heller ha en tjeneste/observer som lytter og flytter til riktig steg/på vent.
            håndterEndretKravgrunnlagFraØkonomi(behandling, kravgrunnlag431, kravgrunnlagetErGyldig);
        } else {
            kravgrunnlagRepository.lagre(behandlingId, kravgrunnlag431);
            gjenopptaBehandlingTjeneste.fortsettBehandlingMedGrunnlag(behandlingId);
        }
    }

    private void håndterEndretKravgrunnlagFraØkonomi(Behandling behandling, Kravgrunnlag431 kravgrunnlag431, boolean kravgrunnlagetErGyldig) {
        long behandlingId = behandling.getId();
        LOG.info("Mottok endret kravgrunnlag for behandlingId={}", behandlingId);
        kravgrunnlagRepository.lagre(behandlingId, kravgrunnlag431);
        if (!kravgrunnlagetErGyldig) {
            LOG.info("Setter behandling på vent pga kravgrunnlag endret til et ugyldig kravgrunnlag for behandlingId={}", behandlingId);
            behandlingskontrollTjeneste.settBehandlingPåVent(behandling, AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG, BehandlingStegType.TBKGSTEG, LocalDateTime.now().plusDays(7), Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);
        } else {
            var skalVenteTil = halvtRettsgebyrTjeneste.lavFeilutbetalingKanVentePåAutomatiskBehandling(behandlingId, kravgrunnlag431) ?
                AutomatiskSaksbehandlingVurderingTjeneste.ventefristForTilfelleSomKanAutomatiskSaksbehandles(kravgrunnlag431) : null;
            tilbakeførBehandlingTilFaktaSteg(behandling, skalVenteTil);
        }
    }

    public void tilbakeførBehandlingTilFaktaSteg(Behandling behandling, LocalDateTime skalVenteTil) {
        var behandlingId = behandling.getId();
        var erForbiFaktaSteg = behandlingskontrollTjeneste.erStegPassert(behandling, FAKTA_FEILUTBETALING);
        // forutsatt at FPTILBAKE allerede har fått SPER melding for den behandlingen og sett behandling på vent med VenteÅrsak VENT_PÅ_TILBAKEKREVINGSGRUNNLAG
        if (erForbiFaktaSteg) {
            LOG.info("Hopper tilbake til {} pga endret kravgrunnlag for behandlingId={}", FAKTA_FEILUTBETALING.getKode(), behandlingId);
            var kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
            behandlingskontrollTjeneste.taBehandlingAvVentSetAlleAutopunktUtført(behandling, kontekst);
            behandlingskontrollTjeneste.behandlingTilbakeføringTilTidligereBehandlingSteg(kontekst, FAKTA_FEILUTBETALING);
            opprettHistorikkinnslagForBehandlingStartetForfra(behandling);

        }
        slettVLAnsvarlingSaksbehandler(behandling); // Hvis AS er ikke null vil den aldri bli plukket opp av automatisk saksbehandling igjen.
        fyrKravgrunnlagEndretEvent(behandlingId);
        if (skalVenteTil == null || skalVenteTil.isBefore(LocalDateTime.now())) {
            gjenopptaBehandlingTjeneste.fortsettBehandlingMedGrunnlag(behandlingId);
        } else {
            behandlingskontrollTjeneste.settBehandlingPåVent(behandling, AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG,
                BehandlingStegType.TBKGSTEG, skalVenteTil, Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);
        }
    }

    private void slettVLAnsvarlingSaksbehandler(Behandling behandling) {
        if (behandling.isAutomatiskSaksbehandlet() && "VL".equals(behandling.getAnsvarligSaksbehandler())) {
            var behandlingLås = behandlingRepository.taSkriveLås(behandling);
            behandling.setAnsvarligSaksbehandler(null);
            behandlingRepository.lagre(behandling, behandlingLås);
        }
    }

    private void fyrKravgrunnlagEndretEvent(Long behandlingId) {
        LOG.info("Sletter gammel grunnlag data for behandlingId={}", behandlingId);
        kravgrunnlagEventPubliserer.fireKravgrunnlagEndretEvent(behandlingId);
    }

    private void opprettHistorikkinnslagForBehandlingStartetForfra(Behandling behandling) {
        var historikkinnslag = lagHistorikkinnslag(behandling);
        var historikkinnslag2 = lagHistorikkinnslag2(behandling);
        historikkRepository.lagre(historikkinnslag, historikkinnslag2);
    }

    private static Historikkinnslag2 lagHistorikkinnslag2(Behandling behandling) {
        return new Historikkinnslag2.Builder()
            .medAktør(HistorikkAktør.VEDTAKSLØSNINGEN)
            .medBehandlingId(behandling.getId())
            .medFagsakId(behandling.getFagsakId())
            .medTittel("Behandling startet forfra")
            .addLinje("Tilbakekreving startes forfra på grunn av endring i feilutbetalt beløp og/eller perioder")
            .build();
    }

    private static Historikkinnslag lagHistorikkinnslag(Behandling behandling) {
        var historikkinnslag = new Historikkinnslag();
        historikkinnslag.setType(HistorikkinnslagType.BEH_STARTET_FORFRA);
        historikkinnslag.setAktør(HistorikkAktør.VEDTAKSLØSNINGEN);
        var historikkInnslagTekstBuilder = new HistorikkInnslagTekstBuilder()
            .medHendelse(HistorikkinnslagType.BEH_STARTET_FORFRA)
            .medBegrunnelse(BEGRUNNELSE_BEHANDLING_STARTET_FORFRA);
        historikkInnslagTekstBuilder.build(historikkinnslag);
        historikkinnslag.setBehandling(behandling);
        return historikkinnslag;
    }
}
