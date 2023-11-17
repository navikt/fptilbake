package no.nav.foreldrepenger.tilbakekreving.behandling.steg.beregn.migrer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.iverksett.TilbakekrevingVedtakDTO;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.iverksett.TilbakekrevingsbelopDTO;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.iverksett.TilbakekrevingsperiodeDTO;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningsresultatMapper;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningsresultatTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.beregningsresultat.BeregningsresultatRepository;
import no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste.TilbakekrevingsvedtakTjeneste;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.MeldingType;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiSendtXmlRepository;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateSegmentCombinator;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.tilbakekreving.tilbakekrevingsvedtak.vedtak.v1.Tilbakekrevingsbelop;
import no.nav.tilbakekreving.tilbakekrevingsvedtak.vedtak.v1.Tilbakekrevingsperiode;
import no.nav.tilbakekreving.tilbakekrevingsvedtak.vedtak.v1.Tilbakekrevingsvedtak;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.log.mdc.MdcExtendedLogContext;

@ApplicationScoped
@ProsessTask("migrer.beregningsresultat")
public class MigrerBeregningsresultatTask implements ProsessTaskHandler {

    private static final MdcExtendedLogContext LOG_CONTEXT = MdcExtendedLogContext.getContext("prosess"); //$NON-NLS-1$
    private static final Logger LOG = LoggerFactory.getLogger(MigrerBeregningsresultatTask.class);

    private BeregningsresultatRepository beregningsresultatRepository;
    private BeregningsresultatTjeneste beregningsresultatTjeneste;
    private BehandlingRepository behandlingRepository;
    private TilbakekrevingsvedtakTjeneste tilbakekrevingsvedtakTjeneste;
    private ØkonomiSendtXmlRepository økonomiSendtXmlRepository;

    public MigrerBeregningsresultatTask() {
        //for CDI proxy
    }

    @Inject
    public MigrerBeregningsresultatTask(BeregningsresultatRepository beregningsresultatRepository,
                                        BeregningsresultatTjeneste beregningsresultatTjeneste,
                                        BehandlingRepository behandlingRepository,
                                        TilbakekrevingsvedtakTjeneste tilbakekrevingsvedtakTjeneste,
                                        ØkonomiSendtXmlRepository økonomiSendtXmlRepository) {
        this.beregningsresultatRepository = beregningsresultatRepository;
        this.beregningsresultatTjeneste = beregningsresultatTjeneste;
        this.behandlingRepository = behandlingRepository;
        this.tilbakekrevingsvedtakTjeneste = tilbakekrevingsvedtakTjeneste;
        this.økonomiSendtXmlRepository = økonomiSendtXmlRepository;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        long behandlingId = Long.parseLong(prosessTaskData.getBehandlingId());
        LOG_CONTEXT.add("behandling", Long.toString(behandlingId));
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        if (behandling.getStatus() != BehandlingStatus.AVSLUTTET) {
            LOG.info("Ingenting å gjøre, behandlingen er ikke avsluttet. Avslutter task.");
            return;
        }
        if (beregningsresultatRepository.hentHvisEksisterer(behandlingId).isPresent()) {
            LOG.info("Ingenting å gjøre, beregningsresultat finnes allerede. Avslutter task");
            return;
        }
        Optional<String> sistSendteVedtak = økonomiSendtXmlRepository.finnSisteXml(behandlingId, MeldingType.VEDTAK);
        if (sistSendteVedtak.isEmpty()) {
            throw new IllegalStateException("Kan ikke migrere beregningsresultat. Fantes ikke noe lagret vedtaksXML.");
        }
        var gjeldendeVedtak = TilbakekrevingsvedtakMarshaller.unmarshall(sistSendteVedtak.get(), behandlingId);

        beregningsresultatTjeneste.beregnOgLagre(behandlingId);
        //hvis verifisering under feiler vil ingenting lagres, siden transaksjonen avbrytes ved exeption

        //hvis gamle vedtak sendt til innkrevingskomponenten ikke reproduseres eksakt, er behandlingen antagelig påvirket
        //av tidligere endringer på avrundingsregler. Da trenger vi å reprodusere beregnigsresultatet slik det ville vært
        //med regler som eksisterte da behandlingen ble iverksatt. (har ikke laget støtte for det p.t.)
        TilbakekrevingVedtakDTO reprodusertVedtak = tilbakekrevingsvedtakTjeneste.lagTilbakekrevingsvedtak(behandlingId);
        var avvik = verifiserLikeVedtak(gjeldendeVedtak, reprodusertVedtak);
        if (!avvik.trim().isEmpty()) {
            if (":skatt:".equals(avvik)) {
                oppdaterSkatt(behandlingId, gjeldendeVedtak, reprodusertVedtak);
                TilbakekrevingVedtakDTO reprodusertVedtak1 = tilbakekrevingsvedtakTjeneste.lagTilbakekrevingsvedtak(behandlingId);
                var avvik1 = verifiserLikeVedtak(gjeldendeVedtak, reprodusertVedtak1);
                if (avvik1.trim().isEmpty()) {
                    return;
                }
            }
            throw new IllegalStateException("Differanse i gjeldende vedtak og reprodusert vedtak for " + avvik);
        }
    }

    private String verifiserLikeVedtak(Tilbakekrevingsvedtak gjeldendeVedtak, TilbakekrevingVedtakDTO reprodusertVedtak) {
        var avvik = "";
        avvik = avvik + verifiserLikeVedtakX(gjeldendeVedtak, reprodusertVedtak, "opprinnelig beløp",
            Tilbakekrevingsbelop::getBelopOpprUtbet, TilbakekrevingsbelopDTO::belopOpprUtbet);
        avvik = avvik + verifiserLikeVedtakX(gjeldendeVedtak, reprodusertVedtak, "nytt beløp",
            Tilbakekrevingsbelop::getBelopNy, TilbakekrevingsbelopDTO::belopNy);
        avvik = avvik + verifiserLikeVedtakX(gjeldendeVedtak, reprodusertVedtak, "tilbakekreves",
            Tilbakekrevingsbelop::getBelopTilbakekreves,
            TilbakekrevingsbelopDTO::belopTilbakekreves);
        avvik = avvik + verifiserLikeVedtakX(gjeldendeVedtak, reprodusertVedtak, "uinnkrevd",
            Tilbakekrevingsbelop::getBelopUinnkrevd, TilbakekrevingsbelopDTO::belopUinnkrevd);
        avvik = avvik + verifiserLikeVedtakX(gjeldendeVedtak, reprodusertVedtak, "skatt",
            Tilbakekrevingsbelop::getBelopSkatt, TilbakekrevingsbelopDTO::belopSkatt);
        avvik = avvik + verifiserLikeVedtak(gjeldendeVedtak, reprodusertVedtak, "renter",
            Tilbakekrevingsperiode::getBelopRenter, TilbakekrevingsperiodeDTO::belopRenter);
        return avvik;
    }

    private String verifiserLikeVedtakX(Tilbakekrevingsvedtak gjeldendeVedtak,
                                      TilbakekrevingVedtakDTO reprodusertVedtak,
                                      String beskrivelse,
                                      Function<Tilbakekrevingsbelop, BigDecimal> gjeldendVerdier,
                                      Function<TilbakekrevingsbelopDTO, BigDecimal> reproduserteVerdier) {
        Function<Tilbakekrevingsperiode, BigDecimal> gjeldendeVerdierFraBeløp = p -> p.getTilbakekrevingsbelop()
            .stream()
            .map(gjeldendVerdier)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        Function<TilbakekrevingsperiodeDTO, BigDecimal> reproduserteVerdierFraBeløp = p -> p.tilbakekrevingsbelop()
            .stream()
            .map(reproduserteVerdier)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return verifiserLikeVedtak(gjeldendeVedtak, reprodusertVedtak, beskrivelse, gjeldendeVerdierFraBeløp, reproduserteVerdierFraBeløp);
    }

    private void oppdaterSkatt(Long behandlingId, Tilbakekrevingsvedtak gjeldendeVedtak, TilbakekrevingVedtakDTO reprodusertVedtak) {
        Function<Tilbakekrevingsperiode, BigDecimal> gjeldendeVerdierFraBeløp = p -> p.getTilbakekrevingsbelop()
            .stream()
            .map(Tilbakekrevingsbelop::getBelopSkatt)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        Function<TilbakekrevingsperiodeDTO, BigDecimal> reproduserteVerdierFraBeløp = p -> p.tilbakekrevingsbelop()
            .stream()
            .map(TilbakekrevingsbelopDTO::belopSkatt)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        LocalDateTimeline<BigDecimal> gjeldendeVerdier = hentVerdierFraGjeldendeVedtak(gjeldendeVedtak, gjeldendeVerdierFraBeløp);
        LocalDateTimeline<BigDecimal> reproduserteVerdier = hentVerdierFraReprodusertVedtak(reprodusertVedtak, reproduserteVerdierFraBeløp);

        LocalDateSegmentCombinator<BigDecimal, BigDecimal, BigDecimal> subtract = (intervall, lhs, rhs) -> {
            BigDecimal lhsValue = lhs != null ? lhs.getValue() : BigDecimal.ZERO;
            BigDecimal rhsValue = rhs != null ? rhs.getValue() : BigDecimal.ZERO;
            return new LocalDateSegment<>(intervall, lhsValue.subtract(rhsValue));
        };
        LocalDateTimeline<BigDecimal> differanse = gjeldendeVerdier.crossJoin(reproduserteVerdier, subtract)
            .filterValue(b -> b.signum() != 0);

        var beregning = beregningsresultatTjeneste.finnEllerBeregn(behandlingId);
        var endret = false;
        for (var p : beregning.getBeregningResultatPerioder()) {
            var aktuellDiff = differanse.intersection(new LocalDateInterval(p.getPeriode().getFom(), p.getPeriode().getTom()));
            var sumSkatt = aktuellDiff.stream().map(LocalDateSegment::getValue).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (sumSkatt.compareTo(BigDecimal.ZERO) != 0) {
                p.setSkattBeløp(p.getSkattBeløp().add(sumSkatt));
                p.setTilbakekrevingBeløpEtterSkatt(p.getSkattBeløp().subtract(sumSkatt));
                endret = true;
            }
        }
        if (endret) {
            beregningsresultatRepository.lagre(behandlingId, BeregningsresultatMapper.map(beregning));
        }
    }

    private String verifiserLikeVedtak(Tilbakekrevingsvedtak gjeldendeVedtak,
                                     TilbakekrevingVedtakDTO reprodusertVedtak,
                                     String beskrivelse,
                                     Function<Tilbakekrevingsperiode, BigDecimal> gjeldendeVerdierUthenter,
                                     Function<TilbakekrevingsperiodeDTO, BigDecimal> reproduserteVerdierUthenter) {
        LocalDateTimeline<BigDecimal> gjeldendeVerdier = hentVerdierFraGjeldendeVedtak(gjeldendeVedtak, gjeldendeVerdierUthenter);
        LocalDateTimeline<BigDecimal> reproduserteVerdier = hentVerdierFraReprodusertVedtak(reprodusertVedtak, reproduserteVerdierUthenter);

        LocalDateSegmentCombinator<BigDecimal, BigDecimal, BigDecimal> subtract = (intervall, lhs, rhs) -> {
            BigDecimal lhsValue = lhs != null ? lhs.getValue() : BigDecimal.ZERO;
            BigDecimal rhsValue = rhs != null ? rhs.getValue() : BigDecimal.ZERO;
            return new LocalDateSegment<>(intervall, lhsValue.subtract(rhsValue));
        };
        LocalDateTimeline<BigDecimal> differanse = gjeldendeVerdier.crossJoin(reproduserteVerdier, subtract)
            .filterValue(b -> b.signum() != 0);
        if (!differanse.isEmpty()) {
            var diffPretty = prettyPrint(differanse);
            LOG.error("Differanse i gjeldende vedtak og reprodusert vedtak for {}: {}", beskrivelse, diffPretty);
            return ":" + beskrivelse + ":";
        }
        return "";

    }

    private String prettyPrint(LocalDateTimeline<BigDecimal> differanse) {
        return differanse.stream()
            .map(seg -> seg.getFom() + "/" + seg.getTom() + ":" + seg.getValue().toPlainString())
            .reduce((a, b) -> (a + "," + b))
            .orElse("[empty]");
    }

    private static LocalDateTimeline<BigDecimal> hentVerdierFraGjeldendeVedtak(Tilbakekrevingsvedtak gjeldendeVedtak,
                                                                               Function<Tilbakekrevingsperiode, BigDecimal> gjeldendeVerdier) {
        LocalDateSegmentCombinator<BigDecimal, BigDecimal, BigDecimal> summer = (intervall, lhs, rhs) -> new LocalDateSegment<>(intervall,
            lhs.getValue().add(rhs.getValue()));
        List<LocalDateSegment<BigDecimal>> segmenter = gjeldendeVedtak.getTilbakekrevingsperiode().stream()
            .map(p -> new LocalDateSegment<>(toInterval(p.getPeriode()), gjeldendeVerdier.apply(p)))
            .toList();
        return new LocalDateTimeline<>(segmenter, summer)
            .mapValue(v -> v.setScale(0, RoundingMode.UNNECESSARY))
            .compress();
    }

    private static LocalDateTimeline<BigDecimal> hentVerdierFraReprodusertVedtak(TilbakekrevingVedtakDTO reprodusertVedtak,
                                                                                 Function<TilbakekrevingsperiodeDTO, BigDecimal> reproduserteVerdier) {
        LocalDateSegmentCombinator<BigDecimal, BigDecimal, BigDecimal> summer = (intervall, lhs, rhs) -> new LocalDateSegment<>(intervall,
            lhs.getValue().add(rhs.getValue()));
        List<LocalDateSegment<BigDecimal>> segmenter = reprodusertVedtak.tilbakekrevingsperiode().stream()
            .map(p -> new LocalDateSegment<>(p.periode().fom(), p.periode().tom(), reproduserteVerdier.apply(p)))
            .toList();
        return new LocalDateTimeline<>(segmenter, summer)
            .mapValue(v -> v.setScale(0, RoundingMode.UNNECESSARY))
            .compress();
    }

    private static LocalDateInterval toInterval(no.nav.tilbakekreving.typer.v1.Periode periode) {
        LocalDate fom = LocalDate.of(periode.getFom().getYear(), periode.getFom().getMonth(), periode.getFom().getDay());
        LocalDate tom = LocalDate.of(periode.getTom().getYear(), periode.getTom().getMonth(), periode.getTom().getDay());
        return new LocalDateInterval(fom, tom);
    }
}
