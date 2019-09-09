package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

public class VedtaksbrevOverskrift {

    private static final String OVERSKRIFT_VEDTAKSBREV = "Du må betale tilbake ";
    private static final String TITTEL_VEDTAK_TILBAKEBETALING = "Vedtak tilbakebetaling ";
    private static final String TITTEL_VEDTAK_INGEN_TILBAKEBETALING = "Vedtak ingen tilbakebetaling ";

    private VedtaksbrevOverskrift() {
        //for static access
    }

    //Overskrift = overskriften inni brevet som mottakeren/brukeren ser
    public static String finnOverskriftVedtaksbrev(String fagsaktypePåRiktigSpråk) {
        return OVERSKRIFT_VEDTAKSBREV + fagsaktypePåRiktigSpråk;
    }

    public static String finnTittelVedtaksbrev(String fagsaktypenavnBokmål, boolean skalTilbakekreves) {
        if (skalTilbakekreves) {
            return TITTEL_VEDTAK_TILBAKEBETALING + fagsaktypenavnBokmål;
        } else {
            return TITTEL_VEDTAK_INGEN_TILBAKEBETALING + fagsaktypenavnBokmål;
        }
    }

}
