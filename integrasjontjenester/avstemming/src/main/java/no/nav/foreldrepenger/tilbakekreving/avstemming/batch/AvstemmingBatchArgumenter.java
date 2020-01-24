package no.nav.foreldrepenger.tilbakekreving.avstemming.batch;

import static java.time.LocalDate.parse;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import no.nav.foreldrepenger.batch.BatchArgument;
import no.nav.foreldrepenger.batch.BatchArguments;

public class AvstemmingBatchArgumenter extends BatchArguments {

    private static final String DATO_KEY = "dato";

    @BatchArgument(beskrivelse = "Dato på ISO-format (yyyy-MM-dd)")
    private LocalDate dato;

    public AvstemmingBatchArgumenter(Map<String, String> arguments) {
        super(arguments);
    }

    public LocalDate getDato() {
        return dato;
    }

    @Override
    public boolean settParameterVerdien(String key, String value) {
        if (DATO_KEY.equals(key)) {
            dato = parsedato(value);
        }
        return false;
    }

    @Override
    public boolean isValid() {
        return dato != null;
    }

    @Override
    public String toString() {
        return "AvstemmingBatchArgumenter{" +
            "dato=" + dato +
            '}';
    }

    private LocalDate parsedato(String datoString) {
        return Optional.ofNullable(datoString).map(dato -> parse(dato, DateTimeFormatter.ISO_DATE)).orElse(null);
    }
}
