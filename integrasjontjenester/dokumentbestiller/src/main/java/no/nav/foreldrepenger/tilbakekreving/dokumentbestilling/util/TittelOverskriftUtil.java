package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.util;

public class TittelOverskriftUtil {

    private static final String OVERSKRIFT_VARSELBREV = "NAV vurderer om du må betale tilbake ";
    private static final String OVERSKRIFT_VEDTAKSBREV = "Du må betale tilbake ";
    private static final String TITTEL_VEDTAK_TILBAKEBETALING = "Vedtak tilbakebetaling ";
    private static final String TITTEL_VEDTAK_INGEN_TILBAKEBETALING = "Vedtak ingen tilbakebetaling ";
    private static final String TITTEL_VARSEL_TILBAKEBETALING = "Varsel tilbakebetaling ";

    private TittelOverskriftUtil (){
        //for static access
    }

    public static String finnOverskriftVarselbrev(String fagsaktypePåRiktigSpråk) {
        return OVERSKRIFT_VARSELBREV + fagsaktypePåRiktigSpråk;
    }

    //Overskrift = overskriften inni brevet som mottakeren/brukeren ser
    public static String finnOverskriftVedtaksbrev(String fagsaktypePåRiktigSpråk) {
        return OVERSKRIFT_VEDTAKSBREV + fagsaktypePåRiktigSpråk;
    }

    //Tittel = navn på brevet som sendes med til Dokprod og dukker opp i dokumentlista i fpsak. Alltid bokmål.
    public static String finnTittelVarselbrev(String fagsaktypenavnBokmål) {
        return TITTEL_VARSEL_TILBAKEBETALING + fagsaktypenavnBokmål;
    }

    public static String finnTittelVedtaksbrev(String fagsaktypenavnBokmål, boolean skalTilbakekreves) {
        if (skalTilbakekreves) {
            return TITTEL_VEDTAK_TILBAKEBETALING + fagsaktypenavnBokmål;
        } else {
            return TITTEL_VEDTAK_INGEN_TILBAKEBETALING + fagsaktypenavnBokmål;
        }
    }

}
