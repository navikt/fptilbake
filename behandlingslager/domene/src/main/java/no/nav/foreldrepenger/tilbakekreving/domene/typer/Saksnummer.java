package no.nav.foreldrepenger.tilbakekreving.domene.typer;

import java.util.Objects;
import java.util.regex.Pattern;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.diff.IndexKey;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;


/**
 * Saksnummer refererer til saksnummer registret i GSAK.
 */
@Embeddable
public class Saksnummer implements SakId, IndexKey {
    private static final String CHARS = "a-z0-9_:-";

    private static final Pattern VALID = Pattern.compile("^(-?[1-9]|[a-z0])[" + CHARS + "]*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern INVALID = Pattern.compile("[^" + CHARS + "]+", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    @Column(name = "saksnummer")
    private String saksnummer;

    Saksnummer() {
        // for hibernate
    }

    public Saksnummer(String saksnummer) {
        Objects.requireNonNull(saksnummer, "saksnummer");
        if (!VALID.matcher(saksnummer).matches()) {
            // skal ikke skje, funksjonelle feilmeldinger håndteres ikke her.
            throw new IllegalArgumentException(
                    "Ugyldig saksnummer, støtter kun A-Z/0-9/:/-/_ tegn. Var: " + saksnummer.replaceAll(INVALID.pattern(), "?") + " (vasket)");
        }
        this.saksnummer = saksnummer;
    }

    @SuppressWarnings("unused")
    public Saksnummer(String sakId, Fagsystem fagsystem) {
        this(sakId);
        // FIXME (FC): Set fagsystem
    }

    @Override
    public String getIndexKey() {
        return saksnummer;
    }

    public String getVerdi() {
        return saksnummer;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        Saksnummer other = (Saksnummer) obj;
        return Objects.equals(saksnummer, other.saksnummer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(saksnummer);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + saksnummer + ">";
    }

    public static Saksnummer infotrygd(String sakId) {
        return sakId == null ? null : new Saksnummer(sakId, Fagsystem.INFOTRYGD);
    }

    public static Saksnummer arena(String sakId) {
        return sakId == null ? null : new Saksnummer(sakId, Fagsystem.ARENA);
    }
}
