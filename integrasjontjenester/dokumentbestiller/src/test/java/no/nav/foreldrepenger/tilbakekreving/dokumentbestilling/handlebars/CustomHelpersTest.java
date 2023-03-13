package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars;

import static no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.CustomHelpers.KroneFormattererMedTusenskille.medTusenskille;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class CustomHelpersTest {
    @Test
    void skal_ha_riktig_tusenskille() {
        assertThat(medTusenskille(BigDecimal.valueOf(1), " ")).isEqualTo("1");
        assertThat(medTusenskille(BigDecimal.valueOf(12), " ")).isEqualTo("12");
        assertThat(medTusenskille(BigDecimal.valueOf(123), " ")).isEqualTo("123");
        assertThat(medTusenskille(BigDecimal.valueOf(1234), " ")).isEqualTo("1 234");
        assertThat(medTusenskille(BigDecimal.valueOf(12345), " ")).isEqualTo("12 345");
        assertThat(medTusenskille(BigDecimal.valueOf(123456), " ")).isEqualTo("123 456");
        assertThat(medTusenskille(BigDecimal.valueOf(1234567), " ")).isEqualTo("1 234 567");
        assertThat(medTusenskille(BigDecimal.valueOf(12345678), " ")).isEqualTo("12 345 678");
        assertThat(medTusenskille(BigDecimal.valueOf(123456789), " ")).isEqualTo("123 456 789");

        assertThat(medTusenskille(BigDecimal.valueOf(1234567), "\u00A0")).isEqualTo("1\u00A0234\u00A0567");
    }

}
