package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandling.modell.LogiskPeriode;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

public class LogiskPeriodeTjenesteTest {

    private LocalDate uke_1_onsdag = LocalDate.of(2020, 1, 1);
    private LocalDate uke_1_torsdag = uke_1_onsdag.plusDays(1);
    private LocalDate uke_1_fredag = uke_1_onsdag.plusDays(2);
    private LocalDate uke_1_lørdag = uke_1_onsdag.plusDays(3);
    private LocalDate uke_1_søndag = uke_1_onsdag.plusDays(4);
    private LocalDate uke_2_mandag = uke_1_onsdag.plusDays(5);

    @Test
    public void tom_input_skal_gi_tom_output() {
        List<LogiskPeriode> resultat = LogiskPeriodeTjeneste.forDagytelse().utledLogiskPeriode(InputBuilder.builder().build());
        Assertions.assertThat(resultat).isEmpty();
    }

    @Test
    public void en_periode_skal_fortsette_som_samme_periode() {
        Periode periode1 = Periode.of(uke_1_onsdag, uke_1_lørdag);
        SortedMap<Periode, BigDecimal> input = InputBuilder.builder().leggTil(periode1, BigDecimal.ONE).build();
        List<LogiskPeriode> resultat = LogiskPeriodeTjeneste.forDagytelse().utledLogiskPeriode(input);
        Assertions.assertThat(resultat).hasSize(1);
        assertSamme(resultat.get(0), periode1, BigDecimal.ONE);
    }

    @Test
    public void intilliggende_perioder_skal_slås_sammen_og_summere_verdi_for_dagytelse() {
        Periode periode1 = Periode.of(uke_1_onsdag, uke_1_onsdag);
        Periode periode2 = Periode.of(uke_1_torsdag, uke_1_torsdag);
        Periode periode3 = Periode.of(uke_1_fredag, uke_2_mandag);
        SortedMap<Periode, BigDecimal> input = InputBuilder.builder()
            .leggTil(periode1, BigDecimal.ONE)
            .leggTil(periode2, BigDecimal.ONE)
            .leggTil(periode3, BigDecimal.ONE)
            .build();
        List<LogiskPeriode> resultat = LogiskPeriodeTjeneste.forDagytelse().utledLogiskPeriode(input);
        Assertions.assertThat(resultat).hasSize(1);
        assertSamme(resultat.get(0), Periode.of(periode1.getFom(), periode3.getTom()), BigDecimal.valueOf(3));
    }

    @Test
    public void intilliggende_perioder_skal_slås_sammen_og_summere_verdi_for_dag7() {
        Periode periode1 = Periode.of(uke_1_onsdag, uke_1_onsdag);
        Periode periode2 = Periode.of(uke_1_torsdag, uke_1_torsdag);
        Periode periode3 = Periode.of(uke_1_fredag, uke_2_mandag);
        SortedMap<Periode, BigDecimal> input = InputBuilder.builder()
            .leggTil(periode1, BigDecimal.ONE)
            .leggTil(periode2, BigDecimal.ONE)
            .leggTil(periode3, BigDecimal.ONE)
            .build();
        List<LogiskPeriode> resultat = LogiskPeriodeTjeneste.forDag7().utledLogiskPeriode(input);
        Assertions.assertThat(resultat).hasSize(1);
        assertSamme(resultat.get(0), Periode.of(periode1.getFom(), periode3.getTom()), BigDecimal.valueOf(3));
    }

    @Test
    public void perioder_som_er_skilt_med_ukedag_skal_ikke_slås_sammen() {
        Periode periode1 = Periode.of(uke_1_onsdag, uke_1_onsdag);
        Periode periode2 = Periode.of(uke_1_fredag, uke_2_mandag);
        SortedMap<Periode, BigDecimal> input = InputBuilder.builder()
            .leggTil(periode1, BigDecimal.ONE)
            .leggTil(periode2, BigDecimal.ONE)
            .build();
        List<LogiskPeriode> resultat = LogiskPeriodeTjeneste.forDagytelse().utledLogiskPeriode(input);
        Assertions.assertThat(resultat).hasSize(2);
        assertSamme(resultat.get(0), periode1, BigDecimal.ONE);
        assertSamme(resultat.get(1), periode2, BigDecimal.ONE);
    }

    @Test
    public void perioder_som_er_skilt_med_helg_skal_slås_sammen_for_dagytelser() {
        Periode periode1 = Periode.of(uke_1_onsdag, uke_1_fredag);
        Periode periode2 = Periode.of(uke_2_mandag, uke_2_mandag);
        SortedMap<Periode, BigDecimal> input = InputBuilder.builder()
            .leggTil(periode1, BigDecimal.ONE)
            .leggTil(periode2, BigDecimal.ONE)
            .build();
        List<LogiskPeriode> resultat = LogiskPeriodeTjeneste.forDagytelse().utledLogiskPeriode(input);
        Assertions.assertThat(resultat).hasSize(1);
        assertSamme(resultat.get(0), Periode.of(uke_1_onsdag, uke_2_mandag), BigDecimal.valueOf(2));
    }

    @Test
    public void perioder_som_er_skilt_med_helg_skal_ikke_slås_sammen_for_dag7() {
        Periode periode1 = Periode.of(uke_1_onsdag, uke_1_fredag);
        Periode periode2 = Periode.of(uke_2_mandag, uke_2_mandag);
        SortedMap<Periode, BigDecimal> input = InputBuilder.builder()
            .leggTil(periode1, BigDecimal.ONE)
            .leggTil(periode2, BigDecimal.ONE)
            .build();
        List<LogiskPeriode> resultat = LogiskPeriodeTjeneste.forDag7().utledLogiskPeriode(input);
        Assertions.assertThat(resultat).hasSize(2);
        assertSamme(resultat.get(0), periode1, BigDecimal.ONE);
        assertSamme(resultat.get(1), periode2, BigDecimal.ONE);
    }

    @Test
    public void perioder_som_er_skilt_med_en_lørdag_skal_slås_sammen_for_dagytelse() {
        Periode periode1 = Periode.of(uke_1_onsdag, uke_1_fredag);
        Periode periode2 = Periode.of(uke_1_søndag, uke_2_mandag);
        SortedMap<Periode, BigDecimal> input = InputBuilder.builder()
            .leggTil(periode1, BigDecimal.ONE)
            .leggTil(periode2, BigDecimal.ONE)
            .build();
        List<LogiskPeriode> resultat = LogiskPeriodeTjeneste.forDagytelse().utledLogiskPeriode(input);
        Assertions.assertThat(resultat).hasSize(1);
        assertSamme(resultat.get(0), Periode.of(uke_1_onsdag, uke_2_mandag), BigDecimal.valueOf(2));
    }

    @Test
    public void perioder_som_er_skilt_med_en_søndag_skal_slås_sammen_for_dagytelse() {
        Periode periode1 = Periode.of(uke_1_onsdag, uke_1_lørdag);
        Periode periode2 = Periode.of(uke_2_mandag, uke_2_mandag);
        SortedMap<Periode, BigDecimal> input = InputBuilder.builder()
            .leggTil(periode1, BigDecimal.ONE)
            .leggTil(periode2, BigDecimal.ONE)
            .build();
        List<LogiskPeriode> resultat = LogiskPeriodeTjeneste.forDagytelse().utledLogiskPeriode(input);
        Assertions.assertThat(resultat).hasSize(1);
        assertSamme(resultat.get(0), Periode.of(uke_1_onsdag, uke_2_mandag), BigDecimal.valueOf(2));
    }


    static void assertSamme(LogiskPeriode ub, Periode periode, BigDecimal verdi) {
        Assertions.assertThat(Periode.of(ub.getFom(), ub.getTom())).isEqualTo(periode);
        Assertions.assertThat(ub.getFeilutbetaltBeløp()).isEqualByComparingTo(verdi);
    }


    static class InputBuilder {

        private SortedMap<Periode, BigDecimal> input = new TreeMap<>(Periode.COMPARATOR);

        static InputBuilder builder() {
            return new InputBuilder();
        }

        InputBuilder leggTil(Periode periode, BigDecimal verdi) {
            input.put(periode, verdi);
            return this;
        }

        InputBuilder leggTil(LocalDate fom, LocalDate tom, BigDecimal verdi) {
            return leggTil(Periode.of(fom, tom), verdi);
        }

        SortedMap<Periode, BigDecimal> build() {
            return input;
        }

    }


}
