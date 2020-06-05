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
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;

public class KravgrunnlagValidator {

    private static final List<Consumer<Kravgrunnlag431>> VALIDATORER = Arrays.asList(
        KravgrunnlagValidator::validerReferanse,
        KravgrunnlagValidator::validerPeriodeInnenforMåned,
        KravgrunnlagValidator::validerOverlappendePerioder,
        KravgrunnlagValidator::validerSkatt,
        KravgrunnlagValidator::validerPerioderHarFeilutbetalingPostering,
        KravgrunnlagValidator::validerYtelseMotFeilutbetaling,
        KravgrunnlagValidator::validerYtelPosteringTilbakekrevesMotNyttOgOpprinneligUtbetalt
    );

    public static void validerGrunnlag(Kravgrunnlag431 kravgrunnlag) throws UgyldigKravgrunnlagException {
        for (var validator : VALIDATORER) {
            validator.accept(kravgrunnlag);
        }
    }

    private static void validerReferanse(Kravgrunnlag431 kravgrunnlag) {
        Henvisning referanse = kravgrunnlag.getReferanse();
        if (referanse == null || StringUtils.isEmpty(referanse.getVerdi())) {
            throw KravgrunnlagFeil.FACTORY.manglerReferanse(kravgrunnlag.getEksternKravgrunnlagId()).toException();
        }
    }

    private static void validerPeriodeInnenforMåned(Kravgrunnlag431 kravgrunnlag) {
        for (KravgrunnlagPeriode432 periode : kravgrunnlag.getPerioder()) {
            Periode p = periode.getPeriode();
            LocalDate fom = p.getFom();
            LocalDate tom = p.getTom();
            YearMonth fomMåned = YearMonth.of(fom.getYear(), fom.getMonth());
            YearMonth tomMåned = YearMonth.of(tom.getYear(), tom.getMonth());
            if (!fomMåned.equals(tomMåned)) {
                throw KravgrunnlagFeil.FACTORY.periodeIkkInnenforMåned(p).toException();
            }
        }
    }

    private static void validerPerioderHarFeilutbetalingPostering(Kravgrunnlag431 kravgrunnlag) {
        for (KravgrunnlagPeriode432 periode : kravgrunnlag.getPerioder()) {
            if (periode.getKravgrunnlagBeloper433().stream().noneMatch(kgb -> KlasseType.FEIL.equals(kgb.getKlasseType()))) {
                throw KravgrunnlagFeil.FACTORY.manglerKlasseTypeFeil(periode.getPeriode()).toException();
            }
        }
    }

    private static void validerOverlappendePerioder(Kravgrunnlag431 kravgrunnlag) {
        List<Periode> sortertePerioder = kravgrunnlag.getPerioder().stream()
            .map(KravgrunnlagPeriode432::getPeriode)
            .sorted(Comparator.comparing(Periode::getFom))
            .collect(Collectors.toList());
        for (int i = 1; i < sortertePerioder.size(); i++) {
            Periode forrige = sortertePerioder.get(i - 1);
            Periode denne = sortertePerioder.get(i);
            if (!denne.getFom().isAfter(forrige.getTom())) {
                throw KravgrunnlagFeil.FACTORY.overlappendePerioder(forrige, denne).toException();
            }
        }
    }

    private static void validerSkatt(Kravgrunnlag431 kravgrunnlag) {
        Map<YearMonth, List<KravgrunnlagPeriode432>> grupppertPåMåned = kravgrunnlag.getPerioder()
            .stream()
            .collect(Collectors.groupingBy(p -> tilMåned(p.getPeriode())));

        for (Map.Entry<YearMonth, List<KravgrunnlagPeriode432>> entry : grupppertPåMåned.entrySet()) {
            validerSkattForPeriode(entry.getKey(), entry.getValue());
        }
    }

    private static void validerSkattForPeriode(YearMonth måned, List<KravgrunnlagPeriode432> perioder) {
        BigDecimal beløpSkattMnd = null;
        BigDecimal sumSkatt = BigDecimal.ZERO;
        for (KravgrunnlagPeriode432 periode : perioder) {
            if (beløpSkattMnd == null) {
                beløpSkattMnd = periode.getBeløpSkattMnd();
            } else {
                if (beløpSkattMnd.compareTo(periode.getBeløpSkattMnd()) != 0) {
                    throw KravgrunnlagFeil.FACTORY.feilSkatt(måned).toException();
                }
            }
            for (KravgrunnlagBelop433 postering : periode.getKravgrunnlagBeloper433()) {
                if (postering.getSkattProsent() == null) {
                    throw KravgrunnlagFeil.FACTORY.manglerFelt("skattProsent", periode.getPeriode()).toException();
                }
                sumSkatt = sumSkatt.add(postering.getTilbakekrevesBelop().multiply(postering.getSkattProsent()));
            }
        }
        sumSkatt = sumSkatt.divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN);
        if (beløpSkattMnd == null) {
            throw KravgrunnlagFeil.FACTORY.manglerMaksSkatt(måned).toException();
        }
        if (sumSkatt.compareTo(beløpSkattMnd) > 0) {
            throw KravgrunnlagFeil.FACTORY.feilSkatt(måned, beløpSkattMnd, sumSkatt).toException();
        }
    }

    private static void validerYtelseMotFeilutbetaling(Kravgrunnlag431 kravgrunnlag) {
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
                throw KravgrunnlagFeil.FACTORY.feilYtelseEllerFeilutbetaling(periode.getPeriode(), sumTilbakekrevesFraYtelsePosteringer, sumNyttBelopFraFeilposteringer).toException();
            }
        }
    }

    private static void validerYtelPosteringTilbakekrevesMotNyttOgOpprinneligUtbetalt(Kravgrunnlag431 kravgrunnlag) {
        for (KravgrunnlagPeriode432 periode : kravgrunnlag.getPerioder()) {
            for (KravgrunnlagBelop433 kgBeløp : periode.getKravgrunnlagBeloper433()) {
                if (KlasseType.YTEL.equals(kgBeløp.getKlasseType())) {
                    BigDecimal diff = kgBeløp.getOpprUtbetBelop().subtract(kgBeløp.getNyBelop());
                    if (kgBeløp.getTilbakekrevesBelop().compareTo(diff) > 0) {
                        throw KravgrunnlagFeil.FACTORY.ytelPosteringHvorTilbakekrevesIkkeStemmerMedNyttOgOpprinneligBeløp(periode.getPeriode(), kgBeløp.getTilbakekrevesBelop(), kgBeløp.getNyBelop(), kgBeløp.getOpprUtbetBelop()).toException();
                    }
                }
            }
        }
    }

    private static YearMonth tilMåned(Periode periode) {
        LocalDate fom = periode.getFom();
        return YearMonth.of(fom.getYear(), fom.getMonth());
    }

    public static class UgyldigKravgrunnlagException extends IntegrasjonException {

        public UgyldigKravgrunnlagException(Feil feil) {
            super(feil);
        }
    }

    interface KravgrunnlagFeil extends DeklarerteFeil {
        KravgrunnlagFeil FACTORY = FeilFactory.create(KravgrunnlagFeil.class);

        @IntegrasjonFeil(feilkode = "FPT-879715", feilmelding = "Ugyldig kravgrunnlag. Mangler forventet felt %s for periode %s.", logLevel = WARN, exceptionClass = UgyldigKravgrunnlagException.class)
        Feil manglerFelt(String felt, Periode periode);

        @IntegrasjonFeil(feilkode = "FPT-879716", feilmelding = "Ugyldig kravgrunnlag for kravgrunnlagId %s. Mangler referanse.", logLevel = WARN, exceptionClass = UgyldigKravgrunnlagException.class)
        Feil manglerReferanse(String kravgrunnlagId);

        @IntegrasjonFeil(feilkode = "FPT-936521", feilmelding = "Ugyldig kravgrunnlag. Overlappende perioder %s og %s.", logLevel = WARN, exceptionClass = UgyldigKravgrunnlagException.class)
        Feil overlappendePerioder(Periode a, Periode b);

        @IntegrasjonFeil(feilkode = "FPT-727260", feilmelding = "Ugyldig kravgrunnlag. Perioden %s mangler postering med klasseType=FEIL.", logLevel = WARN, exceptionClass = UgyldigKravgrunnlagException.class)
        Feil manglerKlasseTypeFeil(Periode periode);

        @IntegrasjonFeil(feilkode = "FPT-438893", feilmelding = "Ugyldig kravgrunnlag. Perioden %s er ikke innenfor en kalendermåned.", logLevel = WARN, exceptionClass = UgyldigKravgrunnlagException.class)
        Feil periodeIkkInnenforMåned(Periode periode);

        @IntegrasjonFeil(feilkode = "FPT-734548", feilmelding = "Ugyldig kravgrunnlag. Mangler max skatt for måned %s", logLevel = WARN, exceptionClass = UgyldigKravgrunnlagException.class)
        Feil manglerMaksSkatt(YearMonth måned);

        @IntegrasjonFeil(feilkode = "FPT-560295", feilmelding = "Ugyldig kravgrunnlag. For måned %s er opplyses ulike verdier maks skatt i ulike perioder", logLevel = WARN, exceptionClass = UgyldigKravgrunnlagException.class)
        Feil feilSkatt(YearMonth måned);

        @IntegrasjonFeil(feilkode = "FPT-930235", feilmelding = "Ugyldig kravgrunnlag. For måned %s er maks skatt %s, men maks tilbakekreving ganget med skattesats blir %s", logLevel = WARN, exceptionClass = UgyldigKravgrunnlagException.class)
        Feil feilSkatt(YearMonth måned, BigDecimal maxSkatt, BigDecimal maxUtregnbarSkatt);

        @IntegrasjonFeil(feilkode = "FPT-361605", feilmelding = "Ugyldig kravgrunnlag. For periode %s er sum tilkakekreving fra YTEL %s, mens belopNytt i FEIL er %s. Det er forventet at disse er like.", logLevel = WARN, exceptionClass = UgyldigKravgrunnlagException.class)
        Feil feilYtelseEllerFeilutbetaling(Periode periode, BigDecimal sumTilbakekrevingYtel, BigDecimal belopNyttFraFeilpostering);

        @IntegrasjonFeil(feilkode = "FPT-615761", feilmelding = "Ugyldig kravgrunnlag. For perioden %s finnes YTEL-postering med tilbakekrevesBeløp %s som er større enn differanse mellom nyttBeløp %s og opprinneligBeløp %s", logLevel = WARN, exceptionClass = UgyldigKravgrunnlagException.class)
        Feil ytelPosteringHvorTilbakekrevesIkkeStemmerMedNyttOgOpprinneligBeløp(Periode periode, BigDecimal tilbakekrevesBeløp, BigDecimal nyttBeløp, BigDecimal opprinneligBeløp);
    }
}
