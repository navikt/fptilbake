package no.nav.foreldrepenger.tilbakekreving.felles;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;
import java.util.Map;
import java.util.Optional;

public class Satser {

    //se https://www.skatteetaten.no/satser/rettsgebyr/
    private static final Map<Year, BigDecimal> RETTSGEBYR_HISTORISK = Map.ofEntries(
        Map.entry(Year.of(2025), BigDecimal.valueOf(1314)),
        Map.entry(Year.of(2024), BigDecimal.valueOf(1277)),
        Map.entry(Year.of(2023), BigDecimal.valueOf(1243))
    );

    private static final Map<Year, BigDecimal> HALVT_RETTSGEBYR_HISTORISK = Map.ofEntries(
        Map.entry(Year.of(2025), BigDecimal.valueOf(657)),
        Map.entry(Year.of(2024), BigDecimal.valueOf(638)),
        Map.entry(Year.of(2023), BigDecimal.valueOf(622))
    );

    //se https://www.skatteetaten.no/satser/grunnbelopet-i-folketrygden/
    private static final BigDecimal GRUNNBELØP = BigDecimal.valueOf(124028);
    private static final BigDecimal HALVT_GRUNNBELØP = GRUNNBELØP.divide(BigDecimal.valueOf(2), 0, RoundingMode.UP);

    private Satser() {
        // sonar
    }

    public static BigDecimal rettsgebyr(Year år) {
        return Optional.ofNullable(RETTSGEBYR_HISTORISK.get(år))
            .orElseThrow(() -> new IllegalArgumentException("Mangler rettsgebyr for år " + år));
    }

    public static BigDecimal rettsgebyr(Year år, int antall) {
        return rettsgebyr(år).multiply(BigDecimal.valueOf(antall));
    }

    public static BigDecimal halvtRettsgebyr(Year år) {
        return Optional.ofNullable(HALVT_RETTSGEBYR_HISTORISK.get(år))
            .orElseThrow(() -> new IllegalArgumentException("Mangler rettsgebyr for år " + år));
    }


    // TODO: Brukes kun i brev i tilfelle hendelse_undertype INNTEKT_UNDER og SVP_INNTEKT_UNDER. Finnes 2 tilfelle i FAKTA_FEILUTBETALING_PERIODE - først hindre flere.
    public static BigDecimal grunnbeløp() {
        return GRUNNBELØP;
    }

    public static BigDecimal halvtGrunnbeløp() {
        return HALVT_GRUNNBELØP;
    }
}
