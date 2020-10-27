package no.nav.foreldrepenger.tilbakekreving.felles;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class Periode {

    public static final Comparator<Periode> COMPARATOR = Comparator.comparing(Periode::getFom).thenComparing(Periode::getTom);
    private static final DateTimeFormatter DATO_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Column(name = "fom")
    private LocalDate fom;

    @Column(name = "tom")
    private LocalDate tom;

    private Periode() {
    }

    public Periode(LocalDate fom, LocalDate tom) {
        Objects.requireNonNull(fom, "Fra-og-med-dato må være satt");
        Objects.requireNonNull(tom, "Til-og-med-dato må være satt");
        if (tom.isBefore(fom)) {
            throw new IllegalArgumentException("Til-og-med-dato før fra-og-med-dato: " + fom + ">" + tom);
        }
        this.fom = fom;
        this.tom = tom;
    }

    public static Periode of(LocalDate fom, LocalDate tom) {
        return new Periode(fom, tom);
    }

    public static Periode omsluttende(Periode... perioder) {
        LocalDate fom = perioder[0].getFom();
        LocalDate tom = perioder[0].getTom();
        for (int i = 1; i < perioder.length; i++) {
            Periode p = perioder[i];
            if (p.getFom().isBefore(fom)) {
                fom = p.getFom();
            }
            if (p.getTom().isAfter(tom)) {
                tom = p.getTom();
            }
        }
        return Periode.of(fom, tom);
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public boolean overlapper(LocalDate dato) {
        return !dato.isBefore(fom) && !dato.isAfter(tom);
    }

    public boolean overlapper(Periode other) {
        boolean fomBeforeOrEqual = getFom().isBefore(other.getTom()) || getFom().isEqual(other.getTom());
        boolean tomAfterOrEqual = getTom().isAfter(other.getFom()) || getTom().isEqual(other.getFom());
        boolean overlapper = fomBeforeOrEqual && tomAfterOrEqual;
        return overlapper;
    }

    public Optional<Periode> overlap(Periode annen) {
        if (!overlapper(annen)) {
            return Optional.empty();
        } else if (this.isEqual(annen)) {
            return Optional.of(this);
        } else {
            return Optional.of(new Periode(max(getFom(), annen.getFom()), min(getTom(), annen.getTom())));
        }
    }

    public boolean erOmsluttetAv(Periode periode) {
        return !fom.isBefore(periode.getFom()) && !tom.isAfter(periode.getTom());
    }

    public boolean omslutter(Periode periode) {
        return !periode.getFom().isBefore(fom) && !periode.getTom().isAfter(tom);
    }

    public static LocalDate max(LocalDate en, LocalDate to) {
        return en.isAfter(to) ? en : to;
    }

    public static LocalDate min(LocalDate en, LocalDate to) {
        return en.isBefore(to) ? en : to;
    }

    public boolean isEqual(Periode other) {
        return Objects.equals(getFom(), other.getFom())
            && Objects.equals(getTom(), other.getTom());
    }

    @Override
    public String toString() {
        return fom.format(DATO_FORMAT) + "-" + tom.format(DATO_FORMAT);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Periode) {
            return isEqual((Periode) o);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fom, tom);
    }

    public Periode plusDays(int dager) {
        return Periode.of(fom.plusDays(dager), tom.plusDays(dager));
    }
}
