package no.nav.foreldrepenger.tilbakekreving.felles;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HelligdagerTest {

    private static final List<LocalDate> hellidagerÅr2021Fasit = List.of(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 4, 1), LocalDate.of(2021, 4, 2),
        LocalDate.of(2021, 4, 5), LocalDate.of(2021, 5, 13), LocalDate.of(2021, 5, 17),
        LocalDate.of(2021, 5, 24));

    @BeforeEach
    void setUp() {
    }

    @Test
    void sjekkOmHelligdagerErRiktigForÅr2021() {

        List<LocalDate> helligdager2021 = Helligdager.finnBevegeligeHelligdagerUtenHelgPerÅr(2021);

        assertThat(helligdager2021).isEqualTo(hellidagerÅr2021Fasit);
    }

    @Test
    void sjekkAtErHelg() {
        assertThat(Helligdager.erHelligdagEllerHelg(LocalDate.of(2020, 4, 12))).isTrue();
    }

    @Test
    void sjekkAtErHelligdag() {
        assertThat(Helligdager.erHelligdagEllerHelg(LocalDate.of(2020, 4, 10))).isTrue();
    }

    @Test
    void sjekkAtErStengtDag() {
        assertThat(Helligdager.erHelligdagEllerHelg(LocalDate.of(2020, 12, 31))).isTrue();
    }

}
