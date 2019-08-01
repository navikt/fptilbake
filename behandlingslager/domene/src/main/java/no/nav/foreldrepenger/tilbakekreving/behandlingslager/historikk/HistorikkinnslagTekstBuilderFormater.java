package no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

public final class HistorikkinnslagTekstBuilderFormater {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");


    private HistorikkinnslagTekstBuilderFormater() {
    }

    public static <T> String formatString(T verdi) {
        if (verdi == null) {
            return null;
        }
        if (verdi instanceof LocalDate) {
            LocalDate localDate = (LocalDate) verdi;
            return formatDate(localDate);
        }
        if (verdi instanceof Periode) {
            Periode interval = (Periode) verdi;
            return formatDate(interval.getFom()) + " - " + formatDate(interval.getTom());
        }
        return verdi.toString();
    }

    private static String formatDate(LocalDate localDate) {
        return DATE_FORMATTER.format(localDate);
    }
}
