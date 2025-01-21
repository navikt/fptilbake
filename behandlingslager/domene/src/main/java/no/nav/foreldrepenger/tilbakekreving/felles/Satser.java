package no.nav.foreldrepenger.tilbakekreving.felles;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Satser {

    private static final Logger LOG = LoggerFactory.getLogger(Satser.class);

    //se https://www.skatteetaten.no/satser/rettsgebyr/
    private static final Map<Year, BigDecimal> RETTSGEBYR_HISTORISK = Map.ofEntries(
        Map.entry(Year.of(2025), BigDecimal.valueOf(1314)),
        Map.entry(Year.of(2024), BigDecimal.valueOf(1277)),
        Map.entry(Year.of(2023), BigDecimal.valueOf(1243)),
        Map.entry(Year.of(2022), BigDecimal.valueOf(1223)),
        Map.entry(Year.of(2021), BigDecimal.valueOf(1199)),
        Map.entry(Year.of(2020), BigDecimal.valueOf(1172)),
        Map.entry(Year.of(2019), BigDecimal.valueOf(1150))
    );

    private static final Map<Year, BigDecimal> HALVT_RETTSGEBYR_HISTORISK = Map.ofEntries(
        Map.entry(Year.of(2025), BigDecimal.valueOf(657)),
        Map.entry(Year.of(2024), BigDecimal.valueOf(638)),
        Map.entry(Year.of(2023), BigDecimal.valueOf(622)),
        Map.entry(Year.of(2022), BigDecimal.valueOf(611)),
        Map.entry(Year.of(2021), BigDecimal.valueOf(599)),
        Map.entry(Year.of(2020), BigDecimal.valueOf(586)),
        Map.entry(Year.of(2019), BigDecimal.valueOf(575))
    );

    private static final Year SENESTE_ÅR = RETTSGEBYR_HISTORISK.keySet().stream().max(Year::compareTo).orElseThrow();

    //se https://www.skatteetaten.no/satser/grunnbelopet-i-folketrygden/
    private static final BigDecimal GRUNNBELØP = BigDecimal.valueOf(124028);
    private static final BigDecimal HALVT_GRUNNBELØP = GRUNNBELØP.divide(BigDecimal.valueOf(2), 0, RoundingMode.UP);

    private Satser() {
        // sonar
    }

    public static BigDecimal rettsgebyr(Year år) {
        var brukÅr = senesteÅrMedDefinertRettsgebyr(år);
        return Optional.ofNullable(RETTSGEBYR_HISTORISK.get(brukÅr))
            .orElseThrow(() -> new IllegalArgumentException("Mangler rettsgebyr for år " + brukÅr));
    }

    public static BigDecimal rettsgebyr(Year år, int antall) {
        return rettsgebyr(år).multiply(BigDecimal.valueOf(antall));
    }

    public static BigDecimal halvtRettsgebyr(Year år) {
        var brukÅr = senesteÅrMedDefinertRettsgebyr(år);
        return Optional.ofNullable(HALVT_RETTSGEBYR_HISTORISK.get(brukÅr))
            .orElseThrow(() -> new IllegalArgumentException("Mangler rettsgebyr for år " + brukÅr));
    }

    private static Year senesteÅrMedDefinertRettsgebyr(Year år) {
        if (år.isAfter(SENESTE_ÅR)) {
            LOG.warn("Mangler rettsgebyr for år {}. Legg det inn i koden. Med en gang.", år);
            return SENESTE_ÅR;
        }
        return år;
    }


    // TODO: Brukes kun i brev i tilfelle hendelse_undertype INNTEKT_UNDER og SVP_INNTEKT_UNDER. Finnes 2 tilfelle i FAKTA_FEILUTBETALING_PERIODE - først hindre flere.
    public static BigDecimal grunnbeløp() {
        return GRUNNBELØP;
    }

    public static BigDecimal halvtGrunnbeløp() {
        return HALVT_GRUNNBELØP;
    }
}
