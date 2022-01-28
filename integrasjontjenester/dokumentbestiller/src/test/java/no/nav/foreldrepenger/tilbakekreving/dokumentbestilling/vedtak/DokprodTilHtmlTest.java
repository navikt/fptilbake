package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf.DokprodTilHtml;

public class DokprodTilHtmlTest {

    @Test
    public void skal_konvertere_overskrift_og_avsnitt() {
        String resultat = DokprodTilHtml.dokprodInnholdTilHtml(
                "_Overskrift\nFørste avsnitt\n\nAndre avsnitt\n\nTredje avsnitt"
        );
        Assertions.assertThat(resultat).isEqualTo(
                "<div class=\"samepage\"><h2>Overskrift</h2><p>Første avsnitt</p></div><p>Andre avsnitt</p><p>Tredje avsnitt</p>"
        );
    }

    @Test
    public void skal_konvertere_non_break_space() {
        // utf8nonBreakingSpace = "\u00A0";
        String resultat = DokprodTilHtml.dokprodInnholdTilHtml("10\u00A0000\u00A0kroner");

        Assertions.assertThat(resultat).isEqualTo("<p>10&nbsp;000&nbsp;kroner</p>");
    }

    @Test
    public void skal_konvertere_bullet_points() {
        String resultat = DokprodTilHtml.dokprodInnholdTilHtml("*-bulletpoint 1\nbulletpoint 2\nsiste bulletpoint-*");
        Assertions.assertThat(resultat).isEqualTo("<ul><li>bulletpoint 1</li><li>bulletpoint 2</li><li>siste bulletpoint</li></ul>");
    }

    @Test
    public void skal_konvertere_bullet_points_når_første_linje_er_tom() {
        String resultat = DokprodTilHtml.dokprodInnholdTilHtml("*-\nbulletpoint 1\nbulletpoint 2\nsiste bulletpoint-*");
        Assertions.assertThat(resultat).isEqualTo("<ul><li>bulletpoint 1</li><li>bulletpoint 2</li><li>siste bulletpoint</li></ul>");
    }

    @Test
    public void skal_konvertere_halvhjertede_avsnitt() {
        //halvhjertet avsnitt er hvor det er tatt kun ett linjeskift.
        String resultat = DokprodTilHtml.dokprodInnholdTilHtml(
                "Foo\nBar"
        );
        Assertions.assertThat(resultat).isEqualTo(
                "<p>Foo<br/>Bar</p>"
        );
    }

    @Test
    public void skal_spesialbehandle_hilsen() {
        //halvhjertet avsnitt er hvor det er tatt kun ett linjeskift.
        String resultat = DokprodTilHtml.dokprodInnholdTilHtml(
                "Med vennlig hilsen\nNAV Familie- og pensjonsytelser"
        );
        Assertions.assertThat(resultat).isEqualTo(
                "<p class=\"hilsen\">Med vennlig hilsen<br/>NAV Familie- og pensjonsytelser</p>"
        );
    }
}
