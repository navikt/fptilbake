package no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonValue;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.diff.IndexKey;

/**
 * Journalpostid refererer til journalpost registret i Joark.
 */
public record JournalpostId(@JsonValue String verdi) implements Serializable, IndexKey {
    private static final String CHARS = "a-z0-9_:-";

    private static final Pattern VALID = Pattern.compile("^(-?[1-9]|[a-z0])[" + CHARS + "]*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern INVALID = Pattern.compile("[^" + CHARS + "]+", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);


    public JournalpostId {
        Objects.requireNonNull(verdi, "verdi");
        if (!VALID.matcher(verdi).matches()) {
            // skal ikke skje, funksjonelle feilmeldinger håndteres ikke her.
            throw new IllegalArgumentException("Ugyldig journalpostId, støtter kun A-Z/0-9/:/-/_ tegn. Var: " + verdi.replaceAll(INVALID.pattern(), "?") + " (vasket)");
        }
    }

    @Override
    public String getIndexKey() {
        return verdi;
    }

    public String getVerdi() {
        return verdi;
    }

    @Converter
    public static class JournalpostIdConverter implements AttributeConverter<JournalpostId, String> {

        @Override
        public String convertToDatabaseColumn(JournalpostId attribute) {
            return Optional.ofNullable(attribute).map(JournalpostId::getVerdi).orElse(null);
        }

        @Override
        public JournalpostId convertToEntityAttribute(String dbData) {
            return Optional.ofNullable(dbData).map(JournalpostId::new).orElse(null);
        }
    }
}
