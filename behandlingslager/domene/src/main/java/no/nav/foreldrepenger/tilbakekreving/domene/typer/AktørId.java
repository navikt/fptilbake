package no.nav.foreldrepenger.tilbakekreving.domene.typer;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Pattern.Flag;

import com.fasterxml.jackson.annotation.JsonValue;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.diff.IndexKey;


/**
 * Id som genereres fra Nav aktørregister. Denne iden benyttes til interne forhold i Nav og vil ikke endres f.eks. dersom bruker går fra
 * DNR til FNR i Folkeregisteret. Tilsvarende vil den kunne referere personer som har ident fra et utenlandsk system.
 */
@Embeddable
public class AktørId implements Serializable, Comparable<AktørId>, IndexKey {
    private static final String CHARS = "a-z0-9_:-";

    private static final String VALID_REGEXP = "^(-?[1-9]|[a-z0])[" + CHARS + "]*$";
    private static final String INVALID_REGEXP = "[^" + CHARS + "]+";

    private static final Pattern VALID = Pattern.compile(VALID_REGEXP, Pattern.CASE_INSENSITIVE);
    private static final Pattern INVALID = Pattern.compile(INVALID_REGEXP, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    @JsonValue
    @jakarta.validation.constraints.Pattern(regexp = VALID_REGEXP, flags = {Flag.CASE_INSENSITIVE})
    @Column(name = "aktoer_id", updatable = false, length = 50)
    private String aktørId;

    protected AktørId() {
        // for hibernate
    }

    public AktørId(Long aktørId) {
        Objects.requireNonNull(aktørId, "aktørId");
        this.aktørId = aktørId.toString();
    }

    public AktørId(String aktørId) {
        Objects.requireNonNull(aktørId, "aktørId");
        if (!VALID.matcher(aktørId).matches()) {
            // skal ikke skje, funksjonelle feilmeldinger håndteres ikke her.
            throw new IllegalArgumentException("Ugyldig aktørId, støtter kun A-Z/0-9/:/-/_ tegn. Var: " + aktørId.replaceAll(INVALID.pattern(), "?") + " (vasket)");
        }
        this.aktørId = aktørId;
    }

    @Override
    public String getIndexKey() {
        return getId();
    }

    public String getId() {
        return aktørId;
    }

    @Override
    public int compareTo(AktørId o) {
        // TODO: Burde ikke finnes - er er pga LRU-cache
        return aktørId.compareTo(o.aktørId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        AktørId other = (AktørId) obj;
        return Objects.equals(aktørId, other.aktørId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktørId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + aktørId + ">";
    }
}
