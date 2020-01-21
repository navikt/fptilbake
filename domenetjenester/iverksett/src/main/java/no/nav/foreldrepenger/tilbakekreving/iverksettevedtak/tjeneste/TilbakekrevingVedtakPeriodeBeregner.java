package no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningResultatPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.TilbakekrevingBeregningTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BeregningResultat;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.felles.Virkedager;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagBelop433;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.util.Objects;

@ApplicationScoped
public class TilbakekrevingVedtakPeriodeBeregner {

    private static final int GRENSE_AVRUNDINGSFEIL = 5;
    private static final Logger logger = LoggerFactory.getLogger(TilbakekrevingVedtakPeriodeBeregner.class);

    private TilbakekrevingBeregningTjeneste beregningTjeneste;

    TilbakekrevingVedtakPeriodeBeregner() {
        //for CDI proxy
    }

    @Inject
    public TilbakekrevingVedtakPeriodeBeregner(TilbakekrevingBeregningTjeneste beregningTjeneste) {
        this.beregningTjeneste = beregningTjeneste;
    }

    public List<TilbakekrevingPeriode> lagTilbakekrevingsPerioder(Long behandlingId, Kravgrunnlag431 kravgrunnlag431) {
        BeregningResultat beregningResultat = beregningTjeneste.beregn(behandlingId);
        return lagTilbakekrevingsPerioder(kravgrunnlag431, beregningResultat);
    }

    public List<TilbakekrevingPeriode> lagTilbakekrevingsPerioder(Kravgrunnlag431 kravgrunnlag, BeregningResultat beregningResultat) {
        List<KravgrunnlagPeriode432> kgPerioder = sortertePerioder(kravgrunnlag);
        List<BeregningResultatPeriode> brPerioder = sortertePerioder(beregningResultat);
        validerInput(kgPerioder, brPerioder);

        Map<Periode, Integer> kgTidligereBehandledeVirkedager = initVirkedagerMap(kgPerioder);
        Map<YearMonth, BigDecimal> kgGjenståendeMuligSkattetrekk = initMuligSkattetrekk(kgPerioder);

        List<TilbakekrevingPeriode> resultat = new ArrayList<>();
        for (BeregningResultatPeriode bgPeriode : brPerioder) {
            List<TilbakekrevingPeriode> bgResultatPerioder = lagTilbakekrevingPerioder(bgPeriode, kgPerioder, kgTidligereBehandledeVirkedager);
            justerAvrunding(bgPeriode, bgResultatPerioder);
            oppdaterGjenståendeSkattetrekk(bgResultatPerioder, kgGjenståendeMuligSkattetrekk);
            justerAvrundingSkatt(bgPeriode, bgResultatPerioder, kgGjenståendeMuligSkattetrekk);

            leggPåRenter(bgPeriode, bgResultatPerioder);
            leggPåKodeResultat(bgPeriode, bgResultatPerioder);

            resultat.addAll(bgResultatPerioder);
        }
        return resultat;
    }

    private List<TilbakekrevingPeriode> lagTilbakekrevingPerioder(BeregningResultatPeriode bgPeriode, List<KravgrunnlagPeriode432> kgPerioder, Map<Periode, Integer> kgTidligereBehandledeVirkedager) {
        Skalering andelSkalering = Skalering.opprett(bgPeriode.getTilbakekrevingBeløpUtenRenter(), bgPeriode.getFeilutbetaltBeløp());
        List<TilbakekrevingPeriode> resultat = new ArrayList<>();
        for (KravgrunnlagPeriode432 kgPeriode : kgPerioder) {
            if (virkedagerOverlapp(kgPeriode.getPeriode(), bgPeriode.getPeriode()) > 0) {
                resultat.add(lagTilbakekrevingsperiode(bgPeriode, kgTidligereBehandledeVirkedager, andelSkalering, kgPeriode));
            }
        }
        return resultat;
    }

    private TilbakekrevingPeriode lagTilbakekrevingsperiode(BeregningResultatPeriode bgPeriode, Map<Periode, Integer> kgTidligereBehandledeVirkedager, Skalering andelSkalering, KravgrunnlagPeriode432 kgPeriode) {
        Periode kPeriode = kgPeriode.getPeriode();
        int virkedagerOverlapp = virkedagerOverlapp(kPeriode, bgPeriode.getPeriode());
        int kgBehandledeVirkedager = kgTidligereBehandledeVirkedager.get(kPeriode);
        int kgPeriodeVirkedager = Virkedager.beregnAntallVirkedager(kPeriode);
        Skalering kgTidligereSkalering = Skalering.opprett(kgBehandledeVirkedager, kgPeriodeVirkedager);
        Skalering kgKumulativSkalering = Skalering.opprett(kgBehandledeVirkedager + virkedagerOverlapp, kgPeriodeVirkedager);
        kgTidligereBehandledeVirkedager.put(kPeriode, kgBehandledeVirkedager + virkedagerOverlapp);

        TilbakekrevingPeriode tp = new TilbakekrevingPeriode(kPeriode.overlap(bgPeriode.getPeriode()).orElseThrow());

        for (KravgrunnlagBelop433 kgBeløp : kgPeriode.getKravgrunnlagBeloper433()) {
            BigDecimal skalertNyttBeløp = skalerMedAvrundingskorrigering(kgBeløp.getNyBelop(), kgTidligereSkalering, kgKumulativSkalering);
            if (KlasseType.FEIL.equals(kgBeløp.getKlasseType())) {
                tp.medBeløp(new TilbakekrevingBeløp(kgBeløp.getKlasseType(), kgBeløp.getKlasseKode())
                    .medNyttBeløp(skalertNyttBeløp)
                    .medUtbetBeløp(BigDecimal.ZERO)
                    .medTilbakekrevBeløp(BigDecimal.ZERO)
                    .medUinnkrevdBeløp(BigDecimal.ZERO)
                    .medSkattBeløp(BigDecimal.ZERO));
            }
            if (KlasseType.YTEL.equals(kgBeløp.getKlasseType())) {
                BigDecimal skalertUtbet = skalerMedAvrundingskorrigering(kgBeløp.getOpprUtbetBelop(), kgTidligereSkalering, kgKumulativSkalering);
                BigDecimal skalertForeslåttTilbakekreves = skalerMedAvrundingskorrigering(kgBeløp.getTilbakekrevesBelop(), kgTidligereSkalering, kgKumulativSkalering);
                BigDecimal skalertTilbakekreves = skalerMedAvrundingskorrigering(kgBeløp.getTilbakekrevesBelop(), kgTidligereSkalering, kgKumulativSkalering, andelSkalering);
                BigDecimal skattBeløp = beregnSkattBeløp(skalertTilbakekreves, kgBeløp.getSkattProsent());

                tp.medBeløp(new TilbakekrevingBeløp(kgBeløp.getKlasseType(), kgBeløp.getKlasseKode())
                    .medNyttBeløp(skalertNyttBeløp)
                    .medUtbetBeløp(skalertUtbet)
                    .medTilbakekrevBeløp(skalertTilbakekreves)
                    .medUinnkrevdBeløp(skalertForeslåttTilbakekreves.subtract(skalertTilbakekreves))
                    .medSkattBeløp(skattBeløp));
            }
        }
        return tp;
    }

    private void justerAvrunding(BeregningResultatPeriode beregningResultatPeriode, List<TilbakekrevingPeriode> perioder) {
        BigDecimal fasit = beregningResultatPeriode.getTilbakekrevingBeløpUtenRenter();
        BigDecimal sumPerioder = perioder.stream()
            .map(TilbakekrevingVedtakPeriodeBeregner::summerTilbakekreving)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal diff = sumPerioder.subtract(fasit);
        if (diff.signum() == 0) {
            return;
        }

        List<TilbakekrevingBeløp> ytelBeløp = perioder.stream()
            .flatMap(p -> p.getBeløp().stream())
            .filter(b -> KlasseType.YTEL.equals(b.getKlasseType()))
            .collect(Collectors.toList());

        if (diff.signum() < 0) {
            justerOpp(beregningResultatPeriode.getPeriode(), diff, ytelBeløp);
        }
        if (diff.signum() > 0) {
            justerNed(beregningResultatPeriode.getPeriode(), diff, ytelBeløp);
        }
    }

    private void justerOpp(Periode periode, BigDecimal diff, List<TilbakekrevingBeløp> ytelBeløp) {
        int i = 0;
        while (diff.signum() < 0 && i < ytelBeløp.size()) {
            TilbakekrevingBeløp kandidat = ytelBeløp.get(i);
            if (kandidat.getUinnkrevdBeløp().signum() > 0) {
                kandidat.medTilbakekrevBeløp(kandidat.getTilbakekrevBeløp().add(BigDecimal.ONE));
                kandidat.medUinnkrevdBeløp(kandidat.getUinnkrevdBeløp().subtract(BigDecimal.ONE));
                diff = diff.add(BigDecimal.ONE);
            }
            i++;
        }
        if (diff.signum() != 0) {
            rapporterAvrundingsfeil(diff, TilbakekrevingVedtakPeriodeBeregnerFeil.FACTORY.avrundingsfeilForLiteInnkrevet(periode, diff.abs()));
        }
    }

    private void justerNed(Periode periode, BigDecimal diff, List<TilbakekrevingBeløp> ytelBeløp) {
        int i = 0;
        while (diff.signum() > 0 && i < ytelBeløp.size()) {
            TilbakekrevingBeløp kandidat = ytelBeløp.get(i);
            if (kandidat.getTilbakekrevBeløp().signum() > 0) {
                kandidat.medTilbakekrevBeløp(kandidat.getTilbakekrevBeløp().subtract(BigDecimal.ONE));
                kandidat.medUinnkrevdBeløp(kandidat.getUinnkrevdBeløp().add(BigDecimal.ONE));
                diff = diff.subtract(BigDecimal.ONE);
            }
            i++;
        }
        if (diff.signum() != 0) {
            rapporterAvrundingsfeil(diff, TilbakekrevingVedtakPeriodeBeregnerFeil.FACTORY.avrundingsfeilForMyeInnkrevet(periode, diff.abs()));
        }
    }

    private void justerAvrundingSkatt(BeregningResultatPeriode beregningResultatPeriode, List<TilbakekrevingPeriode> perioder, Map<YearMonth, BigDecimal> kgGjenståendeMuligSkattetrekk) {
        BigDecimal diff = finnDifferanseMotForventetSkatt(beregningResultatPeriode, perioder);
        for (TilbakekrevingPeriode kandidatPeriode : perioder) {
            Periode periode = kandidatPeriode.getPeriode();
            for (TilbakekrevingBeløp kandidat : kandidatPeriode.getBeløp()) {
                if (!kandidat.getKlasseType().equals(KlasseType.YTEL)) {
                    continue;
                }
                boolean justerSkattOpp = diff.signum() == -1 && harGjenståendeMuligSkattetrekk(periode, kgGjenståendeMuligSkattetrekk);
                boolean justerSkattNed = diff.signum() == 1 && kandidat.getSkattBeløp().compareTo(BigDecimal.ONE) >= 1;
                if (justerSkattOpp || justerSkattNed) {
                    BigDecimal justering = BigDecimal.valueOf(diff.signum()).negate();
                    kandidat.medSkattBeløp(kandidat.getSkattBeløp().add(justering));
                    justerGjenståendeMuligSkattetrekk(periode, justering.negate(), kgGjenståendeMuligSkattetrekk);
                    diff = diff.add(justering);
                }
            }
        }
        rapporterVedUfullstendigJustering(diff, beregningResultatPeriode.getPeriode());
    }

    BigDecimal finnDifferanseMotForventetSkatt(BeregningResultatPeriode beregningResultatPeriode, List<TilbakekrevingPeriode> perioder) {
        BigDecimal fasit = beregningResultatPeriode.getSkattBeløp();
        BigDecimal sumSkatt = perioder.stream()
            .map(TilbakekrevingVedtakPeriodeBeregner::summerSkatt)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sumSkatt.subtract(fasit);
    }

    private static void rapporterVedUfullstendigJustering(BigDecimal diff, Periode bergningsperiode) {
        if (diff.signum() == 0) {
            return;
        }
        Feil feil = diff.signum() == -1
            ? TilbakekrevingVedtakPeriodeBeregnerFeil.FACTORY.avrundingsfeilForLiteSkatt(bergningsperiode, diff.negate())
            : TilbakekrevingVedtakPeriodeBeregnerFeil.FACTORY.avrundingsfeilForMyeSkatt(bergningsperiode, diff);
        if (diff.abs().intValue() > GRENSE_AVRUNDINGSFEIL) {
            throw feil.toException();
        }
        feil.log(logger);
    }

    private void oppdaterGjenståendeSkattetrekk(List<TilbakekrevingPeriode> perioder, Map<YearMonth, BigDecimal> kgGjenståendeMuligSkattetrekk) {
        //juster gjenstående skattetrekk
        for (TilbakekrevingPeriode tilbakekrevingPeriode : perioder) {
            YearMonth måned = fraPeriode(tilbakekrevingPeriode.getPeriode());
            Optional<BigDecimal> skattBeløp = tilbakekrevingPeriode.getBeløp().stream()
                .filter(b -> KlasseType.YTEL.equals(b.getKlasseType()))
                .map(TilbakekrevingBeløp::getSkattBeløp)
                .reduce(BigDecimal::add);
            if (skattBeløp.isPresent()) {
                BigDecimal gjenstående = kgGjenståendeMuligSkattetrekk.get(måned).subtract(skattBeløp.get());
                kgGjenståendeMuligSkattetrekk.put(måned, gjenstående);
            }
        }
    }

    private static void justerGjenståendeMuligSkattetrekk(Periode periode, BigDecimal diff, Map<YearMonth, BigDecimal> kgGjenståendeMuligSkattetrekk) {
        YearMonth måned = fraPeriode(periode);
        BigDecimal gjenstående = kgGjenståendeMuligSkattetrekk.get(måned);
        if (gjenstående == null) {
            throw new IllegalArgumentException("mangler data for gjenstående mulig skattetrekk for " + periode);
        }
        kgGjenståendeMuligSkattetrekk.put(måned, gjenstående.add(diff));
    }

    private static boolean harGjenståendeMuligSkattetrekk(Periode periode, Map<YearMonth, BigDecimal> kgGjenståendeMuligSkattetrekk) {
        YearMonth måned = fraPeriode(periode);
        BigDecimal gjenstående = kgGjenståendeMuligSkattetrekk.get(måned);
        if (gjenstående == null) {
            throw new IllegalArgumentException("mangler data for gjenstående mulig skattetrekk for " + periode);
        }
        return gjenstående.compareTo(BigDecimal.ONE) >= 0;
    }

    private static void leggPåKodeResultat(BeregningResultatPeriode bgPeriode, List<TilbakekrevingPeriode> tmp) {
        tmp.stream()
            .flatMap(p -> p.getBeløp().stream())
            .forEach(b -> b.medKodeResultat(bgPeriode.getKodeResultat()));
    }

    private static void leggPåRenter(BeregningResultatPeriode bgPeriode, List<TilbakekrevingPeriode> tmp) {
        tmp.forEach(tp -> {
            Skalering vektetOverlapp = Skalering.opprett(summerTilbakekreving(tp), bgPeriode.getTilbakekrevingBeløpUtenRenter());
            tp.medRenter(Skalering.skaler(bgPeriode.getRenteBeløp(), vektetOverlapp));
        });
    }

    private static BigDecimal summerTilbakekreving(TilbakekrevingPeriode tp) {
        return tp.getBeløp().stream().map(TilbakekrevingBeløp::getTilbakekrevBeløp).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal summerSkatt(TilbakekrevingPeriode tp) {
        return tp.getBeløp().stream().map(TilbakekrevingBeløp::getSkattBeløp).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal skalerMedAvrundingskorrigering(BigDecimal verdi, Skalering tidligereSkalering, Skalering kumulativSkalering) {
        BigDecimal tidligereSkalert = Skalering.skaler(verdi, tidligereSkalering);
        BigDecimal kumulativtSkalert = Skalering.skaler(verdi, kumulativSkalering);
        return kumulativtSkalert.subtract(tidligereSkalert);
    }

    private static BigDecimal skalerMedAvrundingskorrigering(BigDecimal verdi, Skalering tidligereSkalering, Skalering kumulativSkalering, Skalering fellesSkalering) {
        BigDecimal tidligereSkalert = Skalering.skaler(verdi, tidligereSkalering, fellesSkalering);
        BigDecimal kumulativtSkalert = Skalering.skaler(verdi, kumulativSkalering, fellesSkalering);
        return kumulativtSkalert.subtract(tidligereSkalert);
    }

    private static Map<Periode, Integer> initVirkedagerMap(List<KravgrunnlagPeriode432> kgPerioder) {
        Map<Periode, Integer> kgBehandledeVirkedager = new HashMap<>();
        kgPerioder.forEach(p -> kgBehandledeVirkedager.put(p.getPeriode(), 0));
        return kgBehandledeVirkedager;
    }

    private static int virkedagerOverlapp(Periode a, Periode b) {
        return a.overlap(b)
            .map(Virkedager::beregnAntallVirkedager)
            .orElse(0);
    }

    private static List<KravgrunnlagPeriode432> sortertePerioder(Kravgrunnlag431 kravgrunnlag) {
        return kravgrunnlag.getPerioder()
            .stream()
            .sorted(Comparator.comparing(o -> o.getPeriode().getFom()))
            .collect(Collectors.toList());
    }

    private static List<BeregningResultatPeriode> sortertePerioder(BeregningResultat beregningResultat) {
        return beregningResultat.getBeregningResultatPerioder()
            .stream()
            .sorted(Comparator.comparing(p -> p.getPeriode().getFom()))
            .collect(Collectors.toList());
    }


    private static void validerInput(List<KravgrunnlagPeriode432> kgPerioder, List<BeregningResultatPeriode> brPerioder) {
        validerKravgrunnlagMotBeregningsresultat(kgPerioder, brPerioder);
        validerBeregningsresultatMotKravgrunnlag(kgPerioder, brPerioder);
    }

    private static void validerBeregningsresultatMotKravgrunnlag(List<KravgrunnlagPeriode432> kgPerioder, List<BeregningResultatPeriode> brPerioder) {
        for (BeregningResultatPeriode brPeriode : brPerioder) {
            int brTotalDager = Virkedager.beregnAntallVirkedager(brPeriode.getPeriode());
            int brOverlappDager = 0;
            for (KravgrunnlagPeriode432 kgPeriode : kgPerioder) {
                Optional<Periode> overlapp = kgPeriode.getPeriode().overlap(brPeriode.getPeriode());
                if (overlapp.isPresent()) {
                    brOverlappDager += Virkedager.beregnAntallVirkedager(overlapp.get());
                }
            }
            if (brTotalDager != brOverlappDager) {
                throw TilbakekrevingVedtakPeriodeBeregnerFeil.FACTORY.inputvalideringFeiletBrPerioderOverlappKgPerioder(brPeriode.getPeriode(), brTotalDager, brOverlappDager).toException();
            }
        }
    }

    private static void validerKravgrunnlagMotBeregningsresultat(List<KravgrunnlagPeriode432> kgPerioder, List<BeregningResultatPeriode> brPerioder) {
        for (KravgrunnlagPeriode432 kgPeriode : kgPerioder) {
            int kgTotalDager = Virkedager.beregnAntallVirkedager(kgPeriode.getPeriode());
            int kgOverlappDager = 0;
            for (BeregningResultatPeriode brPeriode : brPerioder) {
                Optional<Periode> overlapp = kgPeriode.getPeriode().overlap(brPeriode.getPeriode());
                if (overlapp.isPresent()) {
                    kgOverlappDager += Virkedager.beregnAntallVirkedager(overlapp.get());
                }
            }
            if (kgTotalDager != kgOverlappDager) {
                throw TilbakekrevingVedtakPeriodeBeregnerFeil.FACTORY.inputvalideringFeiletKgPerioderOverlappBrPerioder(kgPeriode.getPeriode(), kgTotalDager, kgOverlappDager).toException();
            }
        }
    }

    private static BigDecimal beregnSkattBeløp(BigDecimal bruttoTilbakekrevesBeløp, BigDecimal skattProsent) {
        return bruttoTilbakekrevesBeløp.multiply(skattProsent).divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN);
    }

    private static Map<YearMonth, BigDecimal> initMuligSkattetrekk(List<KravgrunnlagPeriode432> kgPerioder) {
        Map<YearMonth, BigDecimal> resultat = new HashMap<>();
        for (KravgrunnlagPeriode432 periode : kgPerioder) {
            YearMonth måned = fraPeriode(periode.getPeriode());
            BigDecimal eksisterende = resultat.get(måned);
            if (eksisterende != null && eksisterende.compareTo(periode.getBeløpSkattMnd()) != 0) {
                throw new IllegalArgumentException("Ugyldig kravgrunnlag, kravgrunnlaget inneholder ulike verdier for skatt/måned for " + måned);
            }
            resultat.put(måned, periode.getBeløpSkattMnd());
        }
        return resultat;
    }

    private static YearMonth fraPeriode(Periode periode) {
        Objects.check(periode.getFom().getYear() == periode.getTom().getYear()
            && periode.getFom().getMonthValue() == periode.getTom().getMonthValue(), "Kan ikke konvertere " + periode + " til måned, da den strekker seg over flere måneder");
        return YearMonth.of(periode.getFom().getYear(), periode.getFom().getMonthValue());
    }

    private static void rapporterAvrundingsfeil(BigDecimal diff, Feil feil) {
        if (diff.abs().intValue() > GRENSE_AVRUNDINGSFEIL) {
            throw feil.toException();
        }
        feil.log(logger);
    }

    interface TilbakekrevingVedtakPeriodeBeregnerFeil extends DeklarerteFeil {

        TilbakekrevingVedtakPeriodeBeregnerFeil FACTORY = FeilFactory.create(TilbakekrevingVedtakPeriodeBeregnerFeil.class);

        @TekniskFeil(feilkode = "FPT-870164", feilmelding = "Avrundingsfeil i periode %s i vedtak. Krever inn %s for mye for perioden", logLevel = LogLevel.WARN)
        Feil avrundingsfeilForMyeInnkrevet(Periode periode, BigDecimal diff);

        @TekniskFeil(feilkode = "FPT-480533", feilmelding = "Avrundingsfeil i periode %s i vedtak. Krever inn %s for lite for perioden", logLevel = LogLevel.WARN)
        Feil avrundingsfeilForLiteInnkrevet(Periode periode, BigDecimal diff);

        @TekniskFeil(feilkode = "FPT-925291", feilmelding = "Avrundingsfeil i periode %s i vedtak. Skattebeløp er satt %s for lite for perioden", logLevel = LogLevel.WARN)
        Feil avrundingsfeilForLiteSkatt(Periode periode, BigDecimal diff);

        @TekniskFeil(feilkode = "FPT-812610", feilmelding = "Avrundingsfeil i periode %s i vedtak. Skattebeløp er satt %s for høyt for perioden", logLevel = LogLevel.WARN)
        Feil avrundingsfeilForMyeSkatt(Periode periode, BigDecimal diff);

        @TekniskFeil(feilkode = "FPT-685113", feilmelding = "Kravgrunnlagperiode %s har %s virkedager, forventer en-til-en, men ovelapper mot beregningsresultat med %s dager", logLevel = LogLevel.ERROR)
        Feil inputvalideringFeiletKgPerioderOverlappBrPerioder(Periode periode, int kgVirkedager, int overlappVirkedager);

        @TekniskFeil(feilkode = "FPT-745657", feilmelding = "Beregningsresultatperiode %s har %s virkedager, forventer en-til-en, men ovelapper mot kravgrunnlag med %s dager", logLevel = LogLevel.ERROR)
        Feil inputvalideringFeiletBrPerioderOverlappKgPerioder(Periode periode, int kgVirkedager, int overlappVirkedager);

    }
}
