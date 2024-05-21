package no.nav.foreldrepenger.tilbakekreving.grunnlag;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagOmrådeKode;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.felles.Ukedager;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.vedtak.exception.VLException;

public class KravgrunnlagValidator {

    private KravgrunnlagValidator() {
    }

    private static final List<Consumer<Kravgrunnlag431>> VALIDATORER = List.of(
        KravgrunnlagValidator::validerPeriodeInnenforMåned,
        KravgrunnlagValidator::validerOverlappendePerioder,
        KravgrunnlagValidator::validerSkatt,
        KravgrunnlagValidator::validerPerioderHarFeilutbetalingPostering,
        KravgrunnlagValidator::validerPerioderHarYtelPostering,
        KravgrunnlagValidator::validerPerioderHarFeilPosteringMedNegativFeilutbetaltBeløp,
        KravgrunnlagValidator::validerYtelseMotFeilutbetaling,
        KravgrunnlagValidator::validerYtelPosteringTilbakekrevesMotNyttOgOpprinneligUtbetalt,
        KravgrunnlagValidator::validerReferanse,
        KravgrunnlagValidator::validerPeriodeMedFeilutbetalingHarVirkedager
    );

    public static void validerGrunnlag(Kravgrunnlag431 kravgrunnlag) throws UgyldigKravgrunnlagException {
        for (var validator : VALIDATORER) {
            validator.accept(kravgrunnlag);
        }
    }

    private static void validerReferanse(Kravgrunnlag431 kravgrunnlag) {
        Henvisning referanse = kravgrunnlag.getReferanse();
        if (referanse == null || referanse.getVerdi() == null || referanse.getVerdi().isEmpty()) {
            throw KravgrunnlagFeil.manglerReferanse(kravgrunnlag.getEksternKravgrunnlagId());
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
                throw KravgrunnlagFeil.periodeIkkInnenforMåned(kravgrunnlag.getEksternKravgrunnlagId(), p);
            }
        }
    }

    private static void validerPerioderHarFeilutbetalingPostering(Kravgrunnlag431 kravgrunnlag) {
        for (KravgrunnlagPeriode432 periode : kravgrunnlag.getPerioder()) {
            if (periode.getKravgrunnlagBeloper433().stream().noneMatch(kgb -> KlasseType.FEIL.equals(kgb.getKlasseType()))) {
                throw KravgrunnlagFeil.manglerKlasseTypeFeil(kravgrunnlag.getEksternKravgrunnlagId(), periode.getPeriode());
            }
        }
    }

    private static void validerPerioderHarYtelPostering(Kravgrunnlag431 kravgrunnlag) {
        for (KravgrunnlagPeriode432 periode : kravgrunnlag.getPerioder()) {
            if (periode.getKravgrunnlagBeloper433().stream().noneMatch(kgb -> KlasseType.YTEL.equals(kgb.getKlasseType()))) {
                throw KravgrunnlagFeil.manglerKlasseTypeYtel(kravgrunnlag.getEksternKravgrunnlagId(), periode.getPeriode());
            }
        }
    }

    private static void validerOverlappendePerioder(Kravgrunnlag431 kravgrunnlag) {
        List<Periode> sortertePerioder = kravgrunnlag.getPerioder().stream()
            .map(KravgrunnlagPeriode432::getPeriode)
            .sorted(Comparator.comparing(Periode::getFom))
            .toList();
        for (int i = 1; i < sortertePerioder.size(); i++) {
            Periode forrige = sortertePerioder.get(i - 1);
            Periode denne = sortertePerioder.get(i);
            if (!denne.getFom().isAfter(forrige.getTom())) {
                throw KravgrunnlagFeil.overlappendePerioder(kravgrunnlag.getEksternKravgrunnlagId(), forrige, denne);
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
                    throw KravgrunnlagFeil.feilSkatt(måned);
                }
            }
            for (KravgrunnlagBelop433 postering : periode.getKravgrunnlagBeloper433()) {
                if (postering.getSkattProsent() == null) {
                    throw KravgrunnlagFeil.manglerFelt("skattProsent", periode.getPeriode());
                }
                sumSkatt = sumSkatt.add(postering.getTilbakekrevesBelop().multiply(postering.getSkattProsent()));
            }
        }
        sumSkatt = sumSkatt.divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN);
        if (beløpSkattMnd == null) {
            throw KravgrunnlagFeil.manglerMaksSkatt(måned);
        }
        if (sumSkatt.compareTo(beløpSkattMnd) > 0) {
            throw KravgrunnlagFeil.feilSkatt(måned, beløpSkattMnd, sumSkatt);
        }
    }

    private static void validerPerioderHarFeilPosteringMedNegativFeilutbetaltBeløp(Kravgrunnlag431 kravgrunnlag) {
        for (KravgrunnlagPeriode432 periode : kravgrunnlag.getPerioder()) {
            for (KravgrunnlagBelop433 belop433 : periode.getKravgrunnlagBeloper433()) {
                if (KlasseType.FEIL.equals(belop433.getKlasseType()) && belop433.getNyBelop().compareTo(BigDecimal.ZERO) < 0) {
                    throw KravgrunnlagFeil.feilBeløp(kravgrunnlag.getEksternKravgrunnlagId(), periode.getPeriode());
                }
            }
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
                throw KravgrunnlagFeil.feilYtelseEllerFeilutbetaling(kravgrunnlag.getEksternKravgrunnlagId(), periode.getPeriode(),
                    sumTilbakekrevesFraYtelsePosteringer, sumNyttBelopFraFeilposteringer);
            }
        }
    }

    private static void validerYtelPosteringTilbakekrevesMotNyttOgOpprinneligUtbetalt(Kravgrunnlag431 kravgrunnlag) {
        for (KravgrunnlagPeriode432 periode : kravgrunnlag.getPerioder()) {
            for (KravgrunnlagBelop433 kgBeløp : periode.getKravgrunnlagBeloper433()) {
                if (KlasseType.YTEL.equals(kgBeløp.getKlasseType())) {
                    BigDecimal diff = kgBeløp.getOpprUtbetBelop().subtract(kgBeløp.getNyBelop());
                    if (kgBeløp.getTilbakekrevesBelop().compareTo(diff) > 0) {
                        throw KravgrunnlagFeil.ytelPosteringHvorTilbakekrevesIkkeStemmerMedNyttOgOpprinneligBeløp(
                            kravgrunnlag.getEksternKravgrunnlagId(),
                            periode.getPeriode(),
                            kgBeløp.getTilbakekrevesBelop(),
                            kgBeløp.getNyBelop(),
                            kgBeløp.getOpprUtbetBelop());
                    }
                }
            }
        }
    }

    private static void validerPeriodeMedFeilutbetalingHarVirkedager(Kravgrunnlag431 kravgrunnlag) {
        if (kravgrunnlag.getFagOmrådeKode() == FagOmrådeKode.ENGANGSSTØNAD) {
            return; //OK med engangssønad i helg
        }
        for (KravgrunnlagPeriode432 periode : kravgrunnlag.getPerioder()) {
            if (Ukedager.beregnAntallVirkedager(periode.getPeriode()) == 0) {
                for (KravgrunnlagBelop433 kgBeløp : periode.getKravgrunnlagBeloper433()) {
                    if (KlasseType.YTEL.equals(kgBeløp.getKlasseType()) && kgBeløp.getTilbakekrevesBelop().signum() != 0) {
                        throw KravgrunnlagFeil.feilutbetaltPeriodeUtenVirkedager(
                            kravgrunnlag.getEksternKravgrunnlagId(),
                            periode.getPeriode(),
                            kgBeløp.getTilbakekrevesBelop());
                    }
                }
            }
        }
    }

    private static YearMonth tilMåned(Periode periode) {
        LocalDate fom = periode.getFom();
        return YearMonth.of(fom.getYear(), fom.getMonth());
    }

    public static class UgyldigKravgrunnlagException extends VLException {
        public UgyldigKravgrunnlagException(String kode, String message) {
            super(kode, message, null);
        }
    }

    public static class KravgrunnlagFeil {

        static UgyldigKravgrunnlagException manglerFelt(String felt, Periode periode) {
            return new UgyldigKravgrunnlagException("FPT-879715",
                String.format("Ugyldig kravgrunnlag. Mangler forventet felt %s for periode %s.", felt, periode));
        }

        static UgyldigKravgrunnlagException manglerReferanse(String kravgrunnlagId) {
            return new UgyldigKravgrunnlagException("FPT-879716",
                String.format("Ugyldig kravgrunnlag for kravgrunnlagId %s. Mangler referanse.", kravgrunnlagId));
        }

        static UgyldigKravgrunnlagException overlappendePerioder(String kravgrunnlagId, Periode a, Periode b) {
            return new UgyldigKravgrunnlagException("FPT-936521",
                String.format("Ugyldig kravgrunnlag for kravgrunnlagId %s. Overlappende perioder %s og %s.", kravgrunnlagId, a, b));
        }

        static UgyldigKravgrunnlagException manglerKlasseTypeFeil(String kravgrunnlagId, Periode periode) {
            return new UgyldigKravgrunnlagException("FPT-727260",
                String.format("Ugyldig kravgrunnlag for kravgrunnlagId %s. Perioden %s mangler postering med klasseType=FEIL.", kravgrunnlagId,
                    periode));
        }

        static UgyldigKravgrunnlagException manglerKlasseTypeYtel(String kravgrunnlagId, Periode periode) {
            return new UgyldigKravgrunnlagException("FPT-727261",
                String.format("Ugyldig kravgrunnlag for kravgrunnlagId %s. Perioden %s mangler postering med klasseType=YTEL.", kravgrunnlagId,
                    periode));
        }

        static UgyldigKravgrunnlagException periodeIkkInnenforMåned(String kravgrunnlagId, Periode periode) {
            return new UgyldigKravgrunnlagException("FPT-438893",
                String.format("Ugyldig kravgrunnlag for kravgrunnlagId %s. Perioden %s er ikke innenfor en kalendermåned.", kravgrunnlagId, periode));
        }

        static UgyldigKravgrunnlagException manglerMaksSkatt(YearMonth måned) {
            return new UgyldigKravgrunnlagException("FPT-734548", String.format("Ugyldig kravgrunnlag. Mangler max skatt for måned %s", måned));
        }

        static UgyldigKravgrunnlagException feilSkatt(YearMonth måned) {
            return new UgyldigKravgrunnlagException("FPT-560295",
                String.format("Ugyldig kravgrunnlag. For måned %s er opplyses ulike verdier maks skatt i ulike perioder", måned));
        }

        static UgyldigKravgrunnlagException feilSkatt(YearMonth måned, BigDecimal maxSkatt, BigDecimal maxUtregnbarSkatt) {
            return new UgyldigKravgrunnlagException("FPT-930235",
                String.format("Ugyldig kravgrunnlag. For måned %s er maks skatt %s, men maks tilbakekreving ganget med skattesats blir %s", måned,
                    maxSkatt, maxUtregnbarSkatt));
        }

        static UgyldigKravgrunnlagException feilYtelseEllerFeilutbetaling(String kravgrunnlagId,
                                                                          Periode periode,
                                                                          BigDecimal sumTilbakekrevingYtel,
                                                                          BigDecimal belopNyttFraFeilpostering) {
            return new UgyldigKravgrunnlagException("FPT-361605", String.format(
                "Ugyldig kravgrunnlag for kravgrunnlagId %s. For periode %s er sum tilkakekreving fra YTEL %s, mens belopNytt i FEIL er %s. Det er forventet at disse er like.",
                kravgrunnlagId, periode, sumTilbakekrevingYtel, belopNyttFraFeilpostering));
        }

        static UgyldigKravgrunnlagException ytelPosteringHvorTilbakekrevesIkkeStemmerMedNyttOgOpprinneligBeløp(String kravgrunnlagId,
                                                                                                               Periode periode,
                                                                                                               BigDecimal tilbakekrevesBeløp,
                                                                                                               BigDecimal nyttBeløp,
                                                                                                               BigDecimal opprinneligBeløp) {
            return new UgyldigKravgrunnlagException("FPT-615761", String.format(
                "Ugyldig kravgrunnlag for kravgrunnlagId %s. For perioden %s finnes YTEL-postering med tilbakekrevesBeløp %s som er større enn differanse mellom nyttBeløp %s og opprinneligBeløp %s",
                kravgrunnlagId, periode, tilbakekrevesBeløp, nyttBeløp, opprinneligBeløp));
        }

        static UgyldigKravgrunnlagException feilutbetaltPeriodeUtenVirkedager(String kravgrunnlagId,
                                                                              Periode periode,
                                                                              BigDecimal tilbakekrevesBeløp) {
            return new UgyldigKravgrunnlagException("FPT-25803", String.format(
                "Ugyldig kravgrunnlag for kravgrunnlagId %s. For perioden %s som kun inneholder helg, finnes YTEL-postering med tilbakekrevesBeløp %s som er større enn 0",
                kravgrunnlagId, periode, tilbakekrevesBeløp));
        }

        static UgyldigKravgrunnlagException feilBeløp(String kravgrunnlagId, Periode periode) {
            return new UgyldigKravgrunnlagException("FPT-930247",
                String.format("Ugyldig kravgrunnlag for kravgrunnlagId %s. Perioden %s har FEIL postering med negativ beløp", kravgrunnlagId,
                    periode));
        }
    }
}
