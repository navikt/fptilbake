package no.nav.foreldrepenger.tilbakekreving.behandlingslager.beregningsresultat;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public class BeregningsresultatBuilder {

    private VedtakResultatType vedtakResultatType;
    private List<BeregningsresultatPeriodeEntitet> perioder;

    public BeregningsresultatBuilder medVedtakResultatType(VedtakResultatType vedtakResultatType) {
        this.vedtakResultatType = vedtakResultatType;
        return this;
    }

    public BeregningsresultatBuilder medPerioder(List<BeregningsresultatPeriodeEntitet> perioder) {
        this.perioder = perioder.stream()
            .sorted(Comparator.comparing(v -> v.getPeriode().getFom()))
            .toList();
        return this;
    }

    public BeregningsresultatEntitet build() {
        Objects.requireNonNull(vedtakResultatType, "vedtakResultatType");
        Objects.requireNonNull(perioder, "perioder");
        validerIngenOverlapp();
        return new BeregningsresultatEntitet(vedtakResultatType, perioder);
    }

    private void validerIngenOverlapp() {
        LocalDateTimeline<Integer> antallSamtidigePerioder = new LocalDateTimeline<>(perioder.stream()
            .map(p -> new LocalDateSegment<>(p.getPeriode().getFom(), p.getPeriode().getTom(), 1))
            .toList(), (intervall, lhs, rhs) -> new LocalDateSegment<>(intervall, lhs.getValue() + rhs.getValue()));
        LocalDateTimeline<Integer> overlappendePerioder = antallSamtidigePerioder.filterValue(antall -> antall > 1);
        if (!overlappendePerioder.isEmpty()) {
            List<Periode> p = overlappendePerioder.stream()
                .map(segment -> new Periode(segment.getFom(), segment.getTom()))
                .toList();
            throw new IllegalArgumentException("Overlappende beregningsresultat for følgende perioder: " + p);
        }
    }

}
