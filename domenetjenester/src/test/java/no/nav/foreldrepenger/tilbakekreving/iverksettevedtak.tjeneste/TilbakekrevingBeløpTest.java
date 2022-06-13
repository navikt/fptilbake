package no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;

class TilbakekrevingBeløpTest {

    @Test
    void skal_ha_riktig_oppsett_for_hvilke_klassekoder_som_er_skattepliktige() {
        assertThat(new TilbakekrevingBeløp(KlasseType.YTEL, "FPATORD").erSkattepliktig()).isTrue();
        assertThat(new TilbakekrevingBeløp(KlasseType.YTEL, "FPATAL").erSkattepliktig()).isTrue();
        assertThat(new TilbakekrevingBeløp(KlasseType.YTEL, "FPATFRI").erSkattepliktig()).isTrue();
        assertThat(new TilbakekrevingBeløp(KlasseType.YTEL, "FPATSJO").erSkattepliktig()).isTrue();

        assertThat(new TilbakekrevingBeløp(KlasseType.YTEL, "FPADATORD").erSkattepliktig()).isTrue();
        assertThat(new TilbakekrevingBeløp(KlasseType.YTEL, "FPSVATORD").erSkattepliktig()).isTrue();
        assertThat(new TilbakekrevingBeløp(KlasseType.YTEL, "PNBSATORD").erSkattepliktig()).isTrue();
        assertThat(new TilbakekrevingBeløp(KlasseType.YTEL, "PPNPATORD").erSkattepliktig()).isTrue();
        assertThat(new TilbakekrevingBeløp(KlasseType.YTEL, "OMATORD").erSkattepliktig()).isTrue();
        assertThat(new TilbakekrevingBeløp(KlasseType.YTEL, "OPPATORD").erSkattepliktig()).isTrue();

        assertThat(new TilbakekrevingBeløp(KlasseType.YTEL, "FRISINN-FRILANS").erSkattepliktig()).isTrue();

        assertThat(new TilbakekrevingBeløp(KlasseType.YTEL, "FPATFER").erSkattepliktig()).isFalse();
        assertThat(new TilbakekrevingBeløp(KlasseType.YTEL, "FPENAD-OP").erSkattepliktig()).isFalse();


    }
}