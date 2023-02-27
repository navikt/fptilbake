package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType.FAKTA_FEILUTBETALING;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.LogiskPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagBelop433;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.SlettGrunnlagEventPubliserer;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;

@ApplicationScoped
public class KravgrunnlagTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(KravgrunnlagTjeneste.class);
    public static final String BEGRUNNELSE_BEHANDLING_STARTET_FORFRA = "Tilbakekreving startes forfra på grunn av endring i feilutbetalt beløp og/eller perioder";

    private KravgrunnlagRepository kravgrunnlagRepository;
    private BehandlingRepository behandlingRepository;
    private HistorikkRepository historikkRepository;
    private GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    private SlettGrunnlagEventPubliserer kravgrunnlagEventPubliserer;


    KravgrunnlagTjeneste() {
        // For CDI
    }

    @Inject
    public KravgrunnlagTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste,
                                BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                                SlettGrunnlagEventPubliserer slettGrunnlagEventPubliserer) {
        this.kravgrunnlagRepository = repositoryProvider.getGrunnlagRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.historikkRepository = repositoryProvider.getHistorikkRepository();
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.gjenopptaBehandlingTjeneste = gjenopptaBehandlingTjeneste;

        this.kravgrunnlagEventPubliserer = slettGrunnlagEventPubliserer;
    }


    public List<LogiskPeriode> utledLogiskPeriode(Long behandlingId) {
        return LogiskPeriodeTjeneste.utledLogiskPeriode(finnFeilutbetalingPrPeriode(behandlingId));
    }

    private SortedMap<Periode, BigDecimal> finnFeilutbetalingPrPeriode(Long behandlingId) {
        Kravgrunnlag431 kravgrunnlag = kravgrunnlagRepository.finnKravgrunnlag(behandlingId);
        return mapFeilutbetalingPrPeriode(kravgrunnlag);
    }

    private SortedMap<Periode, BigDecimal> mapFeilutbetalingPrPeriode(Kravgrunnlag431 kravgrunnlag) {
        SortedMap<Periode, BigDecimal> feilutbetalingPrPeriode = new TreeMap<>(Periode.COMPARATOR);
        for (KravgrunnlagPeriode432 kravgrunnlagPeriode432 : kravgrunnlag.getPerioder()) {
            BigDecimal feilutbetalt = kravgrunnlagPeriode432.getKravgrunnlagBeloper433().stream()
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
        Optional<Kravgrunnlag431> kravgrunnlagOpt = kravgrunnlagRepository.finnKravgrunnlagOpt(behandlingId);
        if (kravgrunnlagOpt.isPresent()) {
            SortedMap<Periode, BigDecimal> periodisert = mapFeilutbetalingPrPeriode(kravgrunnlagOpt.get());
            LocalDate fom = periodisert.keySet().stream().map(Periode::getFom).min(Comparator.naturalOrder()).orElse(null);
            LocalDate tom = periodisert.keySet().stream().map(Periode::getTom).max(Comparator.naturalOrder()).orElse(null);
            Periode periode = periodisert.isEmpty() ? null : new Periode(fom, tom);
            BigDecimal sum = periodisert.values().stream().reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
            return Optional.of(new PeriodeMedBeløp(periode, sum));
        }
        return Optional.empty();
    }


    public void lagreTilbakekrevingsgrunnlagFraØkonomi(Long behandlingId, Kravgrunnlag431 kravgrunnlag431, boolean kravgrunnlagetErGyldig) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        if (KravStatusKode.ENDRET.equals(kravgrunnlag431.getKravStatusKode())) {
            //TODO KravgrunnlagTjeneste bør ikke være ansvarlig for å bytte steg/sette på vent. Bør heller ha en tjeneste/observer som lytter og flytter til riktig steg/på vent.
            håndterEndretkravgrunnlagFraØkonomi(behandling, kravgrunnlag431, kravgrunnlagetErGyldig);
        } else {
            kravgrunnlagRepository.lagre(behandlingId, kravgrunnlag431);
            gjenopptaBehandlingTjeneste.fortsettBehandlingMedGrunnlag(behandlingId);
        }
    }

    private void håndterEndretkravgrunnlagFraØkonomi(Behandling behandling, Kravgrunnlag431 kravgrunnlag431, boolean kravgrunnlagetErGyldig) {
        long behandlingId = behandling.getId();
        logger.info("Mottok endret kravgrunnlag for behandlingId={}", behandlingId);
        kravgrunnlagRepository.lagre(behandlingId, kravgrunnlag431);
        if (!kravgrunnlagetErGyldig) {
            logger.info("Setter behandling på vent pga kravgrunnlag endret til et ugyldig kravgrunnlag for behandlingId={}", behandlingId);
            behandlingskontrollTjeneste.settBehandlingPåVent(behandling, AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG, BehandlingStegType.TBKGSTEG, LocalDateTime.now().plusDays(7), Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);
        } else {
            tilbakeførBehandlingTilFaktaSteg(behandling);
        }
    }

    public void tilbakeførBehandlingTilFaktaSteg(Behandling behandling) {
        long behandlingId = behandling.getId();
        boolean erForbiFaktaSteg = behandlingskontrollTjeneste.erStegPassert(behandling, FAKTA_FEILUTBETALING);
        //forutsatt at FPTILBAKE allrede har fått SPER melding for den behandlingen og sett behandling på vent med VenteÅrsak VENT_PÅ_TILBAKEKREVINGSGRUNNLAG
        if (erForbiFaktaSteg) {
            logger.info("Hopper tilbake til {} pga endret kravgrunnlag for behandlingId={}", FAKTA_FEILUTBETALING.getKode(), behandlingId);
            BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
            behandlingskontrollTjeneste.taBehandlingAvVentSetAlleAutopunktUtført(behandling, kontekst);
            behandlingskontrollTjeneste.behandlingTilbakeføringTilTidligereBehandlingSteg(kontekst, FAKTA_FEILUTBETALING);
            opprettHistorikkinnslagForBehandlingStartetForfra(behandling);
        }
        slettVLAnsvarlingSaksbehandler(behandling); // Hvis AS er ikke null vil den aldri bli plukket opp av automatisk sakbehandling igjen.
        fyrKravgrunnlagEndretEvent(behandlingId);
        gjenopptaBehandlingTjeneste.fortsettBehandlingMedGrunnlag(behandlingId);
    }

    private void slettVLAnsvarlingSaksbehandler(Behandling behandling) {
        if (behandling.isAutomatiskSaksbehandlet() && "VL".equals(behandling.getAnsvarligSaksbehandler())) {
            BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
            behandling.setAnsvarligSaksbehandler(null);
            behandlingRepository.lagre(behandling, behandlingLås);
        }
    }

    private void fyrKravgrunnlagEndretEvent(Long behandlingId) {
        logger.info("Sletter gammel grunnlag data for behandlingId={}", behandlingId);
        kravgrunnlagEventPubliserer.fireKravgrunnlagEndretEvent(behandlingId);
    }

    private void opprettHistorikkinnslagForBehandlingStartetForfra(Behandling behandling) {
        Historikkinnslag historikkinnslag = new Historikkinnslag();
        historikkinnslag.setType(HistorikkinnslagType.BEH_STARTET_FORFRA);
        historikkinnslag.setAktør(HistorikkAktør.VEDTAKSLØSNINGEN);
        HistorikkInnslagTekstBuilder historikkInnslagTekstBuilder = new HistorikkInnslagTekstBuilder()
            .medHendelse(HistorikkinnslagType.BEH_STARTET_FORFRA)
            .medBegrunnelse(BEGRUNNELSE_BEHANDLING_STARTET_FORFRA);
        historikkInnslagTekstBuilder.build(historikkinnslag);
        historikkinnslag.setBehandling(behandling);
        historikkRepository.lagre(historikkinnslag);
    }

}
