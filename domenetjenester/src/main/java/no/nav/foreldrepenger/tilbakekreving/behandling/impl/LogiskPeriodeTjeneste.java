package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import no.nav.foreldrepenger.tilbakekreving.behandling.modell.LogiskPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

public class LogiskPeriodeTjeneste {

    private boolean ytelseIHelg;

    LogiskPeriodeTjeneste(boolean ytelseIHelg) {
        this.ytelseIHelg = ytelseIHelg;
    }

    public static LogiskPeriodeTjeneste forDagytelse() {
        return new LogiskPeriodeTjeneste(false);
    }

    public static LogiskPeriodeTjeneste forDag7() {
        return new LogiskPeriodeTjeneste(true);
    }

    public static LogiskPeriodeTjeneste forYtelseType(FagsakYtelseType ytelseType) {
        return new LogiskPeriodeTjeneste(ytelseType == FagsakYtelseType.OMSORGSPENGER || ytelseType == FagsakYtelseType.ENGANGSTØNAD);
    }

    public List<LogiskPeriode> utledLogiskPeriode(SortedMap<Periode, BigDecimal> feilutbetalingPrPeriode) {
        LocalDate førsteDag = null;
        LocalDate sisteDag = null;
        BigDecimal logiskPeriodeBeløp = BigDecimal.ZERO;
        List<LogiskPeriode> resultat = new ArrayList<>();
        for (Map.Entry<Periode, BigDecimal> entry : feilutbetalingPrPeriode.entrySet()) {
            Periode periode = entry.getKey();
            BigDecimal feilutbetaltBeløp = entry.getValue();
            if (førsteDag == null && sisteDag == null) {
                førsteDag = periode.getFom();
                sisteDag = periode.getTom();
            } else {
                if (harYtelsedagerMellom(sisteDag, periode.getFom())) {
                    resultat.add(LogiskPeriode.lagPeriode(førsteDag, sisteDag, logiskPeriodeBeløp));
                    førsteDag = periode.getFom();
                    logiskPeriodeBeløp = BigDecimal.ZERO;
                }
                sisteDag = periode.getTom();
            }
            logiskPeriodeBeløp = logiskPeriodeBeløp.add(feilutbetaltBeløp);
        }
        if (BigDecimal.ZERO.compareTo(logiskPeriodeBeløp) != 0) {
            resultat.add(LogiskPeriode.lagPeriode(førsteDag, sisteDag, logiskPeriodeBeløp));
        }
        return resultat;
    }

    private boolean harYtelsedagerMellom(LocalDate dag1, LocalDate dag2) {
        if (ytelseIHelg) {
            return ChronoUnit.DAYS.between(dag1, dag2) > 1;
        } else {
            return harUkedagerMellom(dag1, dag2);
        }
    }

    private static boolean harUkedagerMellom(LocalDate dag1, LocalDate dag2) {
        if (!dag2.isAfter(dag1)) {
            throw new IllegalArgumentException("dag2 må være etter dag1");
        }
        if (dag1.plusDays(1).equals(dag2)) {
            return false;
        }
        if (dag1.plusDays(2).equals(dag2) && (dag1.getDayOfWeek() == DayOfWeek.FRIDAY || dag1.getDayOfWeek() == DayOfWeek.SATURDAY)) {
            return false;
        }
        if (dag1.plusDays(3).equals(dag2) && dag1.getDayOfWeek() == DayOfWeek.FRIDAY) {
            return false;
        }
        return true;
    }
}
