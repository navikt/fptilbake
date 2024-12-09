package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9;

import java.io.Serializable;
import java.util.Objects;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Id som genereres fra Nav aktørregister. Denne iden benyttes til interne forhold i Nav og vil ikke endres f.eks. dersom bruker går fra
 * DNR til FNR i Folkeregisteret. Tilsvarende vil den kunne referere personer som har ident fra et utenlandsk system.
 */
@JsonFormat(shape = JsonFormat.Shape.STRING)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public class K9AktørId implements Serializable, Comparable<K9AktørId> {

    @JsonValue
    @NotNull
    @Size(max = 20)
    @Pattern(regexp = "^\\d+$", message = "AktørId [${validatedValue}] matcher ikke tillatt pattern [{regexp}]")
    private String aktørId;

    protected K9AktørId() {
        // for hibernate
    }

    public K9AktørId(Long aktørId) {
        this(Objects.requireNonNull(aktørId, "aktørId").toString());
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public K9AktørId(@NotNull @Size(max = 20) @Pattern(regexp = "^\\d+$", message = "AktørId [${validatedValue}] matcher ikke tillatt pattern [{regexp}]") String aktørId) {
        this.aktørId = Objects.requireNonNull(nonEmpty(aktørId), "aktørId");
    }

    private String nonEmpty(String str) {
        return str == null || str.trim().isEmpty() ? null : str.trim();
    }

    public String getId() {
        return aktørId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        K9AktørId other = (K9AktørId) obj;
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

    @Override
    public int compareTo(K9AktørId o) {
        return aktørId.compareTo(o.aktørId);
    }

}
