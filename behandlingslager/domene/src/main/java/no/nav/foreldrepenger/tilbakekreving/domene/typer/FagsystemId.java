package no.nav.foreldrepenger.tilbakekreving.domene.typer;

import java.util.Objects;

public class FagsystemId implements Comparable<FagsystemId> {

    private String saksnummer;
    private int løpenummer;
    private boolean gammeltFormat;

    public static FagsystemId parse(String kode) {
        return kode.contains("-")
                ? parseNyttFormat(kode)
                : parseGammeltFormat(kode);
    }

    private static FagsystemId parseNyttFormat(String kode) {
        int index = kode.lastIndexOf('-');

        FagsystemId id = new FagsystemId();
        id.saksnummer = kode.substring(0, index);
        id.løpenummer = Integer.parseInt(kode.substring(index + 1));
        id.gammeltFormat = false;
        return id;
    }

    private static FagsystemId parseGammeltFormat(String kode) {
        FagsystemId id = new FagsystemId();
        id.saksnummer = kode.substring(0, kode.length() - 3);
        id.løpenummer = Integer.parseInt(kode.substring(kode.length() - 3));
        id.gammeltFormat = true;
        return id;
    }

    private FagsystemId() {
    }

    public Saksnummer getSaksnummer() {
        return new Saksnummer(saksnummer);
    }

    int getLøpenummer() {
        return løpenummer;
    }

    @Override
    public boolean equals(Object annen) {
        if (this == annen) {
            return true;
        }
        if (annen == null || getClass() != annen.getClass()) {
            return false;
        }
        FagsystemId that = (FagsystemId) annen;
        return løpenummer == that.løpenummer &&
                saksnummer.equals(that.saksnummer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(saksnummer, løpenummer);
    }

    @Override
    public String toString() {
        return gammeltFormat
                ? String.format("%s%03d", saksnummer, løpenummer)
                : String.format("%s-%d", saksnummer, løpenummer);
    }

    @Override
    public int compareTo(FagsystemId o) {
        int resultat = saksnummer.compareTo(o.saksnummer);
        if (resultat != 0) {
            return resultat;
        }
        return Long.compare(løpenummer, o.løpenummer);
    }
}
