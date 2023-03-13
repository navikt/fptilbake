package no.nav.foreldrepenger.tilbakekreving.avstemming;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;

class AvstemmingCsvFormatterTest {

    private static final String FORVENTET_HEADER = "avsender;vedtakId;fnr;vedtaksdato;fagsakYtelseType;tilbakekrevesBruttoUtenRenter;skatt;tilbakekrevesNettoUtenRenter;renter;erOmgjøringTilIngenTilbakekreving\n";

    @Test
    void skal_liste_ut_med_forventet_format_for_datoer_og_tall_skal_multipliseres_med_100() {
        AvstemmingCsvFormatter formatter = new AvstemmingCsvFormatter();
        formatter.leggTilRad(testRad());
        assertThat(formatter.getData()).isEqualTo(FORVENTET_HEADER + "fptilbake;1234;12345678901;20191231;FP;1000;200;800;100;");
    }

    @Test
    void skal_ha_newline_for_å_skille_rader() {
        AvstemmingCsvFormatter formatter = new AvstemmingCsvFormatter();
        formatter.leggTilRad(testRad());
        formatter.leggTilRad(testRad());

        String enRad = "fptilbake;1234;12345678901;20191231;FP;1000;200;800;100;";
        assertThat(formatter.getData()).isEqualTo(FORVENTET_HEADER + enRad + "\n" + enRad);
    }

    @Test
    void skal_bruke_kode_i_siste_kolonne_når_det_er_omgjøring_til_ingen_tilbakekreving() {
        AvstemmingCsvFormatter formatter = new AvstemmingCsvFormatter();
        formatter.leggTilRad(testRad()
                .medTilbakekrevesBruttoUtenRenter(BigDecimal.ZERO)
                .medTilbakekrevesNettoUtenRenter(BigDecimal.ZERO)
                .medRenter(BigDecimal.ZERO)
                .medSkatt(BigDecimal.ZERO)
                .medErOmgjøringTilIngenTilbakekreving(true));
        assertThat(formatter.getData()).isEqualTo(FORVENTET_HEADER + "fptilbake;1234;12345678901;20191231;FP;0;0;0;0;Omgjoring0");
    }

    private AvstemmingCsvFormatter.RadBuilder testRad() {
        return AvstemmingCsvFormatter.radBuilder()
                .medAvsender("fptilbake")
                .medVedtakId("1234")
                .medFnr("12345678901")
                .medVedtaksdato(LocalDate.of(2019, 12, 31))
                .medFagsakYtelseType(FagsakYtelseType.FORELDREPENGER)
                .medTilbakekrevesBruttoUtenRenter(BigDecimal.valueOf(1000))
                .medTilbakekrevesNettoUtenRenter(BigDecimal.valueOf(800))
                .medSkatt(BigDecimal.valueOf(200))
                .medRenter(BigDecimal.valueOf(100))
                .medErOmgjøringTilIngenTilbakekreving(false);
    }
}
