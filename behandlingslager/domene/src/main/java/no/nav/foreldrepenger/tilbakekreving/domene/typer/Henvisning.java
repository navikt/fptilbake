package no.nav.foreldrepenger.tilbakekreving.domene.typer;

import java.util.Objects;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import no.nav.vedtak.log.util.LoggerUtils;


@Embeddable
public class Henvisning {
    private static final String CHARS = "a-zA-Z0-9+/";

    private static final Pattern VALID = Pattern.compile("^[" + CHARS + "]*$", Pattern.CASE_INSENSITIVE);

    @Column(name = "henvisning")
    private String henvisning; // NOSONAR

    Henvisning() {
        // for hibernate
    }

    //TODO k9-tilbake denne metoden skal kun brukes i fp-scope. Gå gjennom koden, evt flytt metoden til et fp-spesifikt sted
    public static Henvisning fraEksternBehandlingId(Long eksternBehandlingId) {
        return new Henvisning(Long.toString(eksternBehandlingId));
    }

    public Henvisning(String henvisning) {
        Objects.requireNonNull(henvisning, "henvisning");
        if (!erGyldig(henvisning)) {
            // skal ikke skje, funksjonelle feilmeldinger håndteres ikke her.
            throw new IllegalArgumentException("Ugyldig henvisning, støtter kun " + CHARS + " tegn. Fikk: " + LoggerUtils.removeLineBreaks(henvisning));
        }
        this.henvisning = henvisning;
    }

    public static boolean erGyldig(String henvisning) {
        //TODO k9-tilbake, vurder om denne skal være applikasjons-spesifikk
        return VALID.matcher(henvisning).matches();
    }

    public String getVerdi() {
        return henvisning;
    }

    public long toLong() {
        if (!henvisning.matches("^\\d+$")) {
            throw new IllegalArgumentException("Kan ikke konvertere henvisning " + LoggerUtils.removeLineBreaks(henvisning) + " til long");
        }
        return Long.parseLong(henvisning);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        Henvisning other = (Henvisning) obj;
        return Objects.equals(henvisning, other.henvisning);
    }

    @Override
    public int hashCode() {
        return Objects.hash(henvisning);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + henvisning + ">";
    }
}
