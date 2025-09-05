package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.kodeverk.dto;

import jakarta.validation.constraints.NotNull;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;

import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class KodeverdiSomObjekt<K extends Kodeverdi> {
    @NotNull
    private final K kilde;
    @NotNull
    private final String kodeverk;
    private final String navn;

    public KodeverdiSomObjekt(final K from) {
        this.kilde = Objects.requireNonNull(from);
        this.kodeverk = Objects.requireNonNull(from.getKodeverk());
        this.navn = from.getNavn();
    }

    public K getKilde() {
        return kilde;
    }

    @NotNull
    public String getKode() {
        return Objects.requireNonNull(kilde.getKode());
    }

    public String getKodeverk() {
        return kodeverk;
    }

    public String getNavn() {
        return navn;
    }

    public static <KV extends Kodeverdi> SortedSet<KodeverdiSomObjekt<KV>> sorterte(Set<KV> verdier) {
        final SortedSet<KodeverdiSomObjekt<KV>> result = new TreeSet<>((a, b) -> a.getKode().compareTo(b.getKode()));
        result.addAll(verdier.stream().map(KodeverdiSomObjekt::new).toList());
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof KodeverdiSomObjekt<?> that))
            return false;
        return Objects.equals(kilde, that.kilde) && Objects.equals(kodeverk, that.kodeverk) && Objects.equals(navn, that.navn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kilde, kodeverk, navn);
    }
}
