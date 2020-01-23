package no.nav.foreldrepenger.tilbakekreving.grunnlag;

import static no.nav.vedtak.feil.LogLevel.WARN;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public class KravgrunnlagValidator {

    private static final List<Function<Kravgrunnlag431, Feil>> VALIDATORER = Arrays.asList(
        KravgrunnlagValidator::validerPeriodeInnenforMåned,
        KravgrunnlagValidator::validerOverlappendePerioder,
        KravgrunnlagValidator::validerSkatt,
        KravgrunnlagValidator::validerYtelseMotFeilutbetaling
    );

    public static Optional<Feil> validerGrunnlag(Kravgrunnlag431 kravgrunnlag) {
        for (Function<Kravgrunnlag431, Feil> validator : VALIDATORER) {
            Feil feil = validator.apply(kravgrunnlag);
            if (feil != null) {
                return Optional.of(feil);
            }
        }
        return Optional.empty();
    }

    private static Feil validerPeriodeInnenforMåned(Kravgrunnlag431 kravgrunnlag) {
        for (KravgrunnlagPeriode432 periode : kravgrunnlag.getPerioder()) {
            Periode p = periode.getPeriode();
            LocalDate fom = p.getFom();
            LocalDate tom = p.getTom();
            YearMonth fomMåned = YearMonth.of(fom.getYear(), fom.getMonth());
            YearMonth tomMåned = YearMonth.of(tom.getYear(), tom.getMonth());
            if (!fomMåned.equals(tomMåned)) {
                return KravgrunnlagFeil.FACTORY.periodeIkkInnenforMåned(p);
            }
        }
        return null;
    }

    private static Feil validerOverlappendePerioder(Kravgrunnlag431 kravgrunnlag) {
        List<Periode> sortertePerioder = kravgrunnlag.getPerioder().stream()
            .map(KravgrunnlagPeriode432::getPeriode)
            .sorted(Comparator.comparing(Periode::getFom))
            .collect(Collectors.toList());
        for (int i = 1; i < sortertePerioder.size(); i++) {
            Periode forrige = sortertePerioder.get(i - 1);
            Periode denne = sortertePerioder.get(i);
            if (!denne.getFom().isAfter(forrige.getTom())) {
                return KravgrunnlagFeil.FACTORY.overlappendePerioder(forrige, denne);
            }
        }
        return null;
    }

    private static Feil validerSkatt(Kravgrunnlag431 kravgrunnlag) {
        Map<YearMonth, List<KravgrunnlagPeriode432>> grupppertPåMåned = kravgrunnlag.getPerioder()
            .stream()
            .collect(Collectors.groupingBy(p -> tilMåned(p.getPeriode())));

        for (Map.Entry<YearMonth, List<KravgrunnlagPeriode432>> entry : grupppertPåMåned.entrySet()) {
            Feil feil = validerSkattForPeriode(entry.getKey(), entry.getValue());
            if (feil != null) {
                return feil;
            }
        }
        return null;
    }

    private static Feil validerSkattForPeriode(YearMonth måned, List<KravgrunnlagPeriode432> perioder) {
        BigDecimal beløpSkattMnd = null;
        BigDecimal sumSkatt = BigDecimal.ZERO;
        for (KravgrunnlagPeriode432 periode : perioder) {
            if (beløpSkattMnd == null) {
                beløpSkattMnd = periode.getBeløpSkattMnd();
            } else {
                if (beløpSkattMnd.compareTo(periode.getBeløpSkattMnd()) != 0) {
                    return KravgrunnlagFeil.FACTORY.feilSkatt(måned);
                }
            }
            for (KravgrunnlagBelop433 postering : periode.getKravgrunnlagBeloper433()) {
                sumSkatt = sumSkatt.add(postering.getTilbakekrevesBelop().multiply(postering.getSkattProsent()));
            }
        }
        sumSkatt = sumSkatt.divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN);
        if (beløpSkattMnd == null) {
            return KravgrunnlagFeil.FACTORY.manglerMaksSkatt(måned);
        }
        if (sumSkatt.compareTo(beløpSkattMnd) > 0) {
            return KravgrunnlagFeil.FACTORY.feilSkatt(måned, beløpSkattMnd, sumSkatt);
        }
        return null;
    }

    private static Feil validerYtelseMotFeilutbetaling(Kravgrunnlag431 kravgrunnlag) {
        for (KravgrunnlagPeriode432 periode : kravgrunnlag.getPerioder()) {
            BigDecimal sumTilbakekrevesFraYtelsePosteringer = periode.getKravgrunnlagBeloper433().stream()
                .filter(b -> KlasseType.YTEL.equals(b.getKlasseType()))
                .map(KravgrunnlagBelop433::getTilbakekrevesBelop)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
            BigDecimal sumNyttBelopFraFeilposteringer = periode.getKravgrunnlagBeloper433().stream()
                .filter(b -> KlasseType.FEIL.equals(b.getKlasseType()))
                .map(KravgrunnlagBelop433::getNyBelop)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
            if (sumNyttBelopFraFeilposteringer.compareTo(sumTilbakekrevesFraYtelsePosteringer) != 0) {
                return KravgrunnlagFeil.FACTORY.feilYtelseEllerFeilutbetaling(periode.getPeriode(), sumTilbakekrevesFraYtelsePosteringer, sumNyttBelopFraFeilposteringer);
            }
        }
        return null;
    }

    private static YearMonth tilMåned(Periode periode) {
        LocalDate fom = periode.getFom();
        return YearMonth.of(fom.getYear(), fom.getMonth());
    }

    interface KravgrunnlagFeil extends DeklarerteFeil {
        KravgrunnlagFeil FACTORY = FeilFactory.create(KravgrunnlagFeil.class);

        @TekniskFeil(feilkode = "FPT-936521", feilmelding = "Ugyldig kravgrunnlag. Overlappende perioder %s og %s.", logLevel = WARN)
        Feil overlappendePerioder(Periode a, Periode b);

        @TekniskFeil(feilkode = "FPT-438893", feilmelding = "Ugyldig kravgrunnlag. Perioden %s er ikke innenfor en kalendermåned.", logLevel = WARN)
        Feil periodeIkkInnenforMåned(Periode periode);

        @TekniskFeil(feilkode = "FPT-734548", feilmelding = "Ugyldig kravgrunnlag. Mangler max skatt for måned %s", logLevel = WARN)
        Feil manglerMaksSkatt(YearMonth måned);

        @TekniskFeil(feilkode = "FPT-560295", feilmelding = "Ugyldig kravgrunnlag. For måned %s er opplyses ulike verdier maks skatt i ulike perioder", logLevel = WARN)
        Feil feilSkatt(YearMonth måned);

        @TekniskFeil(feilkode = "FPT-930235", feilmelding = "Ugyldig kravgrunnlag. For måned %s er maks skatt %s, men maks tilbakekreving ganget med skattesats blir %s", logLevel = WARN)
        Feil feilSkatt(YearMonth måned, BigDecimal maxSkatt, BigDecimal maxUtregnbarSkatt);

        @TekniskFeil(feilkode = "FPT-361605", feilmelding = "Ugyldig kravgrunnlag. For periode %s er sum tilkakekreving fra YTEL %s, mens belopNytt i FEIL er %s. Det er forventet at disse er like.", logLevel = WARN)
        Feil feilYtelseEllerFeilutbetaling(Periode periode, BigDecimal sumTilbakekrevingYtel, BigDecimal belopNyttFraFeilpostering);
    }
}
