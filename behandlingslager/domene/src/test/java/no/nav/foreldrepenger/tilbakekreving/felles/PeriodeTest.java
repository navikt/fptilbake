package no.nav.foreldrepenger.tilbakekreving.felles;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

public class PeriodeTest {

    private LocalDate d1 = LocalDate.of(2019, 1, 1);
    private LocalDate d2 = LocalDate.of(2019, 1, 2);
    private LocalDate d3 = LocalDate.of(2019, 1, 3);
    private LocalDate d4 = LocalDate.of(2019, 1, 4);

    @Test
    public void test_av_overlapp() {
        assertThat(Periode.of(d1, d2).overlapper(Periode.of(d1, d2))).isTrue();
        assertThat(Periode.of(d1, d2).overlapper(Periode.of(d1, d3))).isTrue();
        assertThat(Periode.of(d2, d3).overlapper(Periode.of(d1, d4))).isTrue();
        assertThat(Periode.of(d1, d2).overlapper(Periode.of(d3, d4))).isFalse();

        assertThat(Periode.of(d1, d2).overlap(Periode.of(d1, d2))).contains(Periode.of(d1, d2));
        assertThat(Periode.of(d1, d2).overlap(Periode.of(d2, d4))).contains(Periode.of(d2, d2));
        assertThat(Periode.of(d1, d2).overlap(Periode.of(d3, d4))).isEmpty();
    }

    @Test
    public void test_av_omslutter() {
        assertThat(Periode.of(d1, d2).omslutter(Periode.of(d1, d2))).isTrue();
        assertThat(Periode.of(d1, d3).omslutter(Periode.of(d1, d2))).isTrue();
        assertThat(Periode.of(d1, d2).omslutter(Periode.of(d1, d3))).isFalse();

        assertThat(Periode.of(d1, d2).erOmsluttetAv(Periode.of(d1, d2))).isTrue();
        assertThat(Periode.of(d1, d3).erOmsluttetAv(Periode.of(d1, d2))).isFalse();
        assertThat(Periode.of(d1, d2).erOmsluttetAv(Periode.of(d1, d3))).isTrue();
    }

    @Test
    void skal_gi_antall_kalenderdager() {
        assertThat(Periode.of(d1, d1).antallKalenderdager()).isEqualTo(1);
        assertThat(Periode.of(d1, d1.plusDays(6)).antallKalenderdager()).isEqualTo(7);
    }

    @Test
    void skal_gi_antall_ukedager() {
        assertThat(d1.getDayOfWeek()).isEqualTo(DayOfWeek.TUESDAY);
        assertThat(Periode.of(d1, d1).antallUkedager()).isEqualTo(1);
        assertThat(Periode.of(d1, d1.plusDays(6)).antallUkedager()).isEqualTo(5);
    }
}
