package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;

public class DokprodTilHtmlTest {

    @Test
    public void skal_konvertere_overskrift_og_avsnitt() {
        String resultat = DokprodTilHtml.dokprodInnholdTilHtml(
            "_Overskrift\nFørste avsnitt\n\nAndre avsnitt\n\nTredje avsnitt"
        );
        Assertions.assertThat(resultat).isEqualTo(
            "<h2>Overskrift</h2><p>Første avsnitt</p><p>Andre avsnitt</p><p>Tredje avsnitt</p>"
        );
    }

    @Test
    public void skal_konvertere_non_break_space() {
        // utf8nonBreakingSpace = "\u00A0";
        String resultat = DokprodTilHtml.dokprodInnholdTilHtml("10\u00A0000\u00A0kroner");
        Assertions.assertThat(resultat).isEqualTo("<p>10&nbsp;000&nbsp;kroner</p>");
    }

    @Ignore("Ikke implementert støtte enda")
    @Test
    public void skal_konvertere_halvhjertede_avsnitt() {
        //halvhjertet avsnitt er hvor det er tatt kun ett linjeskift.
        String resultat = DokprodTilHtml.dokprodInnholdTilHtml(
            "Med vennlig hilsen\nNAV Familie- og pensjonsytelser"
        );
        Assertions.assertThat(resultat).isEqualTo(
            "<p>Med vennlig hilsen<br/>NAV Familie- og pensjonsytelser</p>"
        );
    }
}
