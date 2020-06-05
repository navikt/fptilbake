package no.nav.foreldrepenger.tilbakekreving.domene.typer;

import java.util.Objects;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import no.nav.vedtak.log.util.LoggerUtils;


@Embeddable
public class Henvisning {
    private static final String CHARS = "a-zA-Z0-9+/";

    //tall som passer i en long
    private static final Pattern LONG_PATTERN = Pattern.compile("^\\d{1,18}$", Pattern.CASE_INSENSITIVE);

    //eksakt 22 tegn base64
    private static final Pattern BASE64_UUID_PATTERN = Pattern.compile("^[" + CHARS + "]{22}$", Pattern.CASE_INSENSITIVE);

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
        this.henvisning = henvisning;
    }

    public static boolean erGyldig(Henvisning henvisning) {
        return erGyldig(henvisning.getVerdi());
    }

    public static boolean erGyldig(String henvisning) {
        //TODO k9-tilbake, vurder om denne skal være applikasjons-spesifikk
        return henvisning != null && (LONG_PATTERN.matcher(henvisning).matches() || BASE64_UUID_PATTERN.matcher(henvisning).matches());
    }

    public String getVerdi() {
        return henvisning;
    }

    public long toLong() {
        if (henvisning == null || !LONG_PATTERN.matcher(henvisning).matches()) {
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
        return henvisning;
    }
}
