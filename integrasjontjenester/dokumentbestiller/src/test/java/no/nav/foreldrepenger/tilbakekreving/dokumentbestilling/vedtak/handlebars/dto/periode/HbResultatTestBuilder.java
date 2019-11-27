package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode;

import java.math.BigDecimal;

public class HbResultatTestBuilder {
    public static HbResultat forTilbakekrevesBeløp(int tilbakekrevesBeløp) {
        return forTilbakekrevesBeløpOgRenter(tilbakekrevesBeløp, 0);
    }

    public static HbResultat forTilbakekrevesBeløpOgRenter(int tilbakekrevesBeløp, int renter) {
        return HbResultat.builder()
            .medTilbakekrevesBeløp(BigDecimal.valueOf(tilbakekrevesBeløp))
            .medRenterBeløp(BigDecimal.valueOf(renter))
            .medTilbakekrevesBeløpUtenSkatt(BigDecimal.valueOf(tilbakekrevesBeløp))
            .build();
    }
}
