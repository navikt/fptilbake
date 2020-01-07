package no.nav.foreldrepenger.tilbakekreving.avstemming;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;

public class AvstemmingCsvFormatterTest {

    @Test
    public void skal_liste_ut_med_forventet_format_for_datoer_og_tall_skal_multipliseres_med_100() {
        AvstemmingCsvFormatter formatter = new AvstemmingCsvFormatter();
        formatter.leggTilRad(testRad());
        //rekkefølge på feltene er: avsender,tidspunkt dannet, vedtaId, fnr, vedtaksdato, ytelsetype,brutto(uten renter), skatt, netto  (uten renter), renter, omgjøringKode
        assertThat(formatter.getData()).isEqualTo("fptilbake;1234;12345678901;20191231;FP;1000;200;800;100;");
    }

    @Test
    public void skal_ha_newline_for_å_skille_rader() {
        AvstemmingCsvFormatter formatter = new AvstemmingCsvFormatter();
        formatter.leggTilRad(testRad());
        formatter.leggTilRad(testRad());

        String enRad = "fptilbake;1234;12345678901;20191231;FP;1000;200;800;100;";
        assertThat(formatter.getData()).isEqualTo(enRad + "\n" + enRad);
    }

    @Test
    public void skal_bruke_kode_i_siste_kolonne_når_det_er_omgjøring_til_ingen_tilbakekreving() {
        AvstemmingCsvFormatter formatter = new AvstemmingCsvFormatter();
        formatter.leggTilRad(testRad()
            .medTilbakekrevesBruttoUtenRenter(BigDecimal.ZERO)
            .medTilbakekrevesNettoUtenRenter(BigDecimal.ZERO)
            .medRenter(BigDecimal.ZERO)
            .medSkatt(BigDecimal.ZERO)
            .medErOmgjøringTilIngenTilbakekreving(true));
        //rekkefølge på feltene er: avsender,tidspunkt dannet, vedtaId, fnr, vedtaksdato, ytelsetype,brutto(uten renter), skatt, netto  (uten renter), renter, omgjøringKode
        assertThat(formatter.getData()).isEqualTo("fptilbake;1234;12345678901;20191231;FP;0;0;0;0;Omgjoring0");
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
