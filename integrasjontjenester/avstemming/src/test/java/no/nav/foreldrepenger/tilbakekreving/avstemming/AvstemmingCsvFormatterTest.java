package no.nav.foreldrepenger.tilbakekreving.avstemming;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;

public class AvstemmingCsvFormatterTest {

    @Test
    public void skal_liste_ut_med_forventet_format_for_datoer_og_tall_skal_multipliseres_med_100() {
        AvstemmingCsvFormatter formatter = new AvstemmingCsvFormatter();
        formatter.leggTilRad(testRad());
        //rekkefølge på feltene er: avsender,tidspunkt dannet, vedtaId, fnr, vedtaksdato, ytelsetype,100*brutto(uten renter), 100*skatt, 100*netto(uten renter), 100*renter
        assertThat(formatter.getData()).isEqualTo("fptilbake,20191231 15:51,1234,12345678901,20191231,FP,100000,20000,80000,10000");
    }

    private AvstemmingCsvFormatter.RadBuilder testRad() {
        return AvstemmingCsvFormatter.radBuilder()
            .medAvsender("fptilbake")
            .medTidspunktDannet(LocalDateTime.of(2019, 12, 31, 15, 51, 14, 141423))
            .medVedtakId("1234")
            .medFnr("12345678901")
            .medVedtaksdato(LocalDate.of(2019, 12, 31))
            .medFagsakYtelseType(FagsakYtelseType.FORELDREPENGER)
            .medTilbakekrevesBruttoUtenRenter(BigDecimal.valueOf(1000))
            .medTilbakekrevesNettoUtenRenter(BigDecimal.valueOf(800))
            .medSkatt(BigDecimal.valueOf(200))
            .medRenter(BigDecimal.valueOf(100));
    }

    @Test
    public void skal_ha_newline_for_å_skille_rader() {
        AvstemmingCsvFormatter formatter = new AvstemmingCsvFormatter();
        formatter.leggTilRad(testRad());
        formatter.leggTilRad(testRad());

        String enRad = "fptilbake,20191231 15:51,1234,12345678901,20191231,FP,100000,20000,80000,10000";
        assertThat(formatter.getData()).isEqualTo(enRad + "\n" + enRad);
    }
}
