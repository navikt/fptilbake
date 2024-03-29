package no.nav.foreldrepenger.tilbakekreving.felles;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UkedagerTest {
    private Map<DayOfWeek, LocalDate> uke;

    @BeforeEach
    void setUp() {
        LocalDate iDag = LocalDate.now();
        LocalDate mandag = iDag.minusDays(iDag.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue());
        uke = Stream.of(DayOfWeek.values()).collect(Collectors.toMap(day -> day, day -> mandag.plusDays(day.ordinal())));
    }

    @Test
    void skalBeregneAntallVirkedager() {
        LocalDate mandag = getDayOfWeek(DayOfWeek.MONDAY);
        LocalDate søndag = getDayOfWeek(DayOfWeek.SUNDAY);

        assertThat(Ukedager.beregnAntallVirkedager(mandag, søndag)).isEqualTo(5);
        assertThat(Ukedager.beregnAntallVirkedager(mandag, søndag.plusDays(1))).isEqualTo(6);
        assertThat(Ukedager.beregnAntallVirkedager(mandag, søndag.plusDays(10))).isEqualTo(13);
        assertThat(Ukedager.beregnAntallVirkedager(mandag.plusDays(1), søndag)).isEqualTo(4);
        assertThat(Ukedager.beregnAntallVirkedager(mandag.plusDays(1), søndag.plusDays(1))).isEqualTo(5);
        assertThat(Ukedager.beregnAntallVirkedager(mandag.plusDays(4), søndag)).isEqualTo(1);
        assertThat(Ukedager.beregnAntallVirkedager(mandag.plusDays(5), søndag)).isEqualTo(0);

        assertThat(Ukedager.beregnAntallVirkedager(mandag.minusDays(1), søndag)).isEqualTo(5);
        assertThat(Ukedager.beregnAntallVirkedager(mandag.minusDays(2), søndag)).isEqualTo(5);
        assertThat(Ukedager.beregnAntallVirkedager(mandag.minusDays(3), søndag)).isEqualTo(6);
        assertThat(Ukedager.beregnAntallVirkedager(mandag.minusDays(3), søndag.plusDays(1))).isEqualTo(7);
    }

    @Test
    void skalLeggeTilVirkedager() {
        LocalDate mandag = getDayOfWeek(DayOfWeek.MONDAY);
        LocalDate tirsdag = getDayOfWeek(DayOfWeek.TUESDAY);
        LocalDate onsdag = getDayOfWeek(DayOfWeek.WEDNESDAY);
        LocalDate fredag = getDayOfWeek(DayOfWeek.FRIDAY);
        LocalDate lørdag = getDayOfWeek(DayOfWeek.SATURDAY);
        LocalDate søndag = getDayOfWeek(DayOfWeek.SUNDAY);
        LocalDate nesteMandag = mandag.plusWeeks(1);
        LocalDate nesteTirsdag = tirsdag.plusWeeks(1);
        LocalDate nesteOnsdag = onsdag.plusWeeks(1);

        assertThat(Ukedager.plusVirkedager(mandag, 1)).isEqualTo(tirsdag);
        assertThat(Ukedager.plusVirkedager(mandag, 4)).isEqualTo(fredag);
        assertThat(Ukedager.plusVirkedager(mandag, 5)).isEqualTo(nesteMandag);

        assertThat(Ukedager.plusVirkedager(tirsdag, 1)).isEqualTo(onsdag);
        assertThat(Ukedager.plusVirkedager(tirsdag, 3)).isEqualTo(fredag);
        assertThat(Ukedager.plusVirkedager(tirsdag, 4)).isEqualTo(nesteMandag);

        assertThat(Ukedager.plusVirkedager(lørdag, 1)).isEqualTo(nesteTirsdag);
        assertThat(Ukedager.plusVirkedager(fredag, 2)).isEqualTo(nesteTirsdag);
        assertThat(Ukedager.plusVirkedager(lørdag, 2)).isEqualTo(nesteOnsdag);

        assertThat(Ukedager.plusVirkedager(søndag, 5)).isEqualTo(søndag.plusWeeks(1).plusDays(1));
    }

    private LocalDate getDayOfWeek(DayOfWeek dayOfWeek) {
        LocalDate date = uke.get(dayOfWeek);
        assertThat(date.getDayOfWeek()).isEqualTo(dayOfWeek);
        return date;
    }
}
