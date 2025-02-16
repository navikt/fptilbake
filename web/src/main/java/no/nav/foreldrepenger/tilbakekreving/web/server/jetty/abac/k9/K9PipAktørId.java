package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonValue;

import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.K9RessursDataValue;

/**
 * Id som genereres fra NAV Aktørregister.
 * Denne iden benyttes til interne forhold i Nav og vil ikke endres f.eks. dersom bruker går fra DNR til FNR i Folkeregisteret.
 */
public class K9PipAktørId implements Serializable, Comparable<K9PipAktørId>, K9RessursDataValue {
    private static final String VALID_REGEXP = "^\\d{13}$";

    private static final Pattern VALID = Pattern.compile(VALID_REGEXP, Pattern.CASE_INSENSITIVE);

    @JsonValue
    @NotNull
    @jakarta.validation.constraints.Pattern(regexp = VALID_REGEXP, message = "aktørId ${validatedValue} har ikke gyldig verdi (pattern '{regexp}')")
    private String aktørId;

    public K9PipAktørId(Long aktørId) {
        Objects.requireNonNull(aktørId, "aktørId");
        this.aktørId = validateAktørId(aktørId.toString());
    }

    public K9PipAktørId(String aktørId) {
        this.aktørId = validateAktørId(aktørId);
    }

    private String validateAktørId(String aktørId) {
        Objects.requireNonNull(aktørId, "aktørId");
        if (!VALID.matcher(aktørId).matches()) {
            // skal ikke skje, funksjonelle feilmeldinger håndteres ikke her.
            throw new IllegalArgumentException("Ugyldig aktørId '" + aktørId + "', tillatt pattern: " + VALID_REGEXP);
        }
        return aktørId;
    }

    @Override
    public String getVerdi() {
        return aktørId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        var other = (K9PipAktørId) obj;
        return Objects.equals(aktørId, other.aktørId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktørId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<maskert>";
    }

    @Override
    public int compareTo(K9PipAktørId o) {
        // TODO: Burde ikke finnes
        return aktørId.compareTo(o.aktørId);
    }

}
