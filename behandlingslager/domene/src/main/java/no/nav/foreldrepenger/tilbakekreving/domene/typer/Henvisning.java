package no.nav.foreldrepenger.tilbakekreving.domene.typer;

import java.io.Serializable;
import java.util.Optional;
import java.util.regex.Pattern;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import no.nav.vedtak.log.util.LoggerUtils;


public record Henvisning(String verdi) implements Serializable {
    private static final String CHARS = "a-zA-Z0-9+/";

    //tall som passer i en long
    private static final Pattern LONG_PATTERN = Pattern.compile("^\\d{1,18}$", Pattern.CASE_INSENSITIVE);

    //eksakt 22 tegn base64
    private static final Pattern BASE64_UUID_PATTERN = Pattern.compile("^[" + CHARS + "]{22}$", Pattern.CASE_INSENSITIVE);

    public static Henvisning fraEksternBehandlingId(Long eksternBehandlingId) {
        return new Henvisning(Long.toString(eksternBehandlingId));
    }


    public static boolean erGyldig(Henvisning henvisning) {
        return Optional.ofNullable(henvisning).map(Henvisning::getVerdi)
            .filter(h -> LONG_PATTERN.matcher(h).matches() || BASE64_UUID_PATTERN.matcher(h).matches())
            .isPresent();
    }

    public String getVerdi() {
        return verdi;
    }

    public long toLong() {
        if (verdi == null || !LONG_PATTERN.matcher(verdi).matches()) {
            throw new IllegalArgumentException("Kan ikke konvertere henvisning " + LoggerUtils.removeLineBreaks(verdi) + " til long");
        }
        return Long.parseLong(verdi);
    }

    // Unngå default med brackets
    @Override
    public String toString() {
        return verdi;
    }


    @Converter
    public static class HenvisningConverter implements AttributeConverter<Henvisning, String> {

        @Override
        public String convertToDatabaseColumn(Henvisning attribute) {
            return Optional.ofNullable(attribute).map(Henvisning::getVerdi).orElse(null);
        }

        @Override
        public Henvisning convertToEntityAttribute(String dbData) {
            return Optional.ofNullable(dbData).map(Henvisning::new).orElse(null);
        }
    }
}
