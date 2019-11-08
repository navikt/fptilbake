package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;

public class VedtaksbrevOverskrift {

    private static final String OVERSKRIFT_VEDTAKSBREV_TILBAKEKREVING = "Du må betale tilbake ";
    private static final String OVERSKRIFT_VEDTAKSBREV_INGEN_TILBAKEKREVING = "Du må ikke betale tilbake ";
    private static final String TITTEL_VEDTAK_TILBAKEBETALING = "Vedtak tilbakebetaling ";
    private static final String TITTEL_VEDTAK_INGEN_TILBAKEBETALING = "Vedtak ingen tilbakebetaling ";

    private VedtaksbrevOverskrift() {
        //for static access
    }

    //Overskrift = overskriften inni brevet som mottakeren/brukeren ser
    public static String finnOverskriftVedtaksbrev(String fagsaktypePåRiktigSpråk, VedtakResultatType hovedresultat) {
        if (VedtakResultatType.FULL_TILBAKEBETALING.equals(hovedresultat) || VedtakResultatType.DELVIS_TILBAKEBETALING.equals(hovedresultat)) {
            return OVERSKRIFT_VEDTAKSBREV_TILBAKEKREVING + fagsaktypePåRiktigSpråk;
        } else if (VedtakResultatType.INGEN_TILBAKEBETALING.equals(hovedresultat)) {
            return OVERSKRIFT_VEDTAKSBREV_INGEN_TILBAKEKREVING + fagsaktypePåRiktigSpråk;
        } else {
            throw new IllegalArgumentException("Utvikler-feil: ikke-støttet VedtakResultatType: " + hovedresultat);
        }
    }

    public static String finnTittelVedtaksbrev(String fagsaktypenavnBokmål, boolean tilbakekreves) {
        if (tilbakekreves) {
            return TITTEL_VEDTAK_TILBAKEBETALING + fagsaktypenavnBokmål;
        } else {
            return TITTEL_VEDTAK_INGEN_TILBAKEBETALING + fagsaktypenavnBokmål;
        }
    }

}
