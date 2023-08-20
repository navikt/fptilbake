package no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.function.Function;

class Skalering<T> {
    private static final String DIVISOR_APPEND_MELDING = " og divisor=";
    private T multiplikator;
    private T divisor;
    private Function<T, BigDecimal> konverterer;
    private static final int DESIMALER = 0;

    public Skalering(T multiplikator, T divisor, Function<T, BigDecimal> konverterer) {
        Objects.requireNonNull(multiplikator, "multiplikator");
        Objects.requireNonNull(divisor, "divisor");
        Objects.requireNonNull(konverterer, "konverterer");
        this.multiplikator = multiplikator;
        this.divisor = divisor;
        this.konverterer = konverterer;
    }

    public static Skalering<BigDecimal> opprett(BigDecimal multiplikator, BigDecimal divisor) {
        if (multiplikator.compareTo(divisor) == 0) {
            return null;
        }
        if (multiplikator.signum() < 0 || divisor.signum() < 0) {
            throw new IllegalArgumentException("Utvikler-feil: forventer ikke negative tall, men fikk multiplikator=" + multiplikator + DIVISOR_APPEND_MELDING + divisor);
        }
        if (multiplikator.compareTo(divisor) > 0) {
            throw new IllegalArgumentException("Utvikler-feil: forventer bare skalering mellom 0 og 1, men fikk multiplikator=" + multiplikator + DIVISOR_APPEND_MELDING + divisor);
        }
        return new Skalering<>(multiplikator, divisor, Function.identity());
    }

    public static Skalering<Integer> opprett(int multiplikator, int divisor) {
        if (multiplikator == divisor) {
            return null;
        }
        if (multiplikator < 0 || divisor < 0) {
            throw new IllegalArgumentException("Utvikler-feil: forventer ikke negative tall, men fikk multiplikator=" + multiplikator + DIVISOR_APPEND_MELDING + divisor);
        }
        if (multiplikator > divisor) {
            throw new IllegalArgumentException("Utvikler-feil: forventer bare skalering mellom 0 og 1, men fikk multiplikator=" + multiplikator + DIVISOR_APPEND_MELDING + divisor);
        }
        return new Skalering<>(multiplikator, divisor, BigDecimal::valueOf);
    }

    public BigDecimal getMultiplikator() {
        return konverterer.apply(multiplikator);
    }

    public BigDecimal getDivisor() {
        return konverterer.apply(divisor);
    }


    public static BigDecimal skaler(BigDecimal verdi, Skalering skalering) {
        if (skalering == null) {
            return verdi;
        }
        return verdi.multiply(skalering.getMultiplikator()).divide(skalering.getDivisor(), DESIMALER, RoundingMode.HALF_UP);
    }

    public static BigDecimal skaler(BigDecimal verdi, Skalering skalering1, Skalering skalering2) {
        if (skalering1 == null) {
            return skaler(verdi, skalering2);
        }
        if (skalering2 == null) {
            return skaler(verdi, skalering1);
        }
        BigDecimal multiplikator = skalering1.getMultiplikator().multiply(skalering2.getMultiplikator());
        BigDecimal divisor = skalering1.getDivisor().multiply(skalering2.getDivisor());
        return verdi.multiply(multiplikator).divide(divisor, DESIMALER, RoundingMode.HALF_UP);
    }

}
