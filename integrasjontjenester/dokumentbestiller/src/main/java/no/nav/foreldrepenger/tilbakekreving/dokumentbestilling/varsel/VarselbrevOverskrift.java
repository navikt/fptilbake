package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel;

public class VarselbrevOverskrift {

    private static final String OVERSKRIFT_VARSELBREV = "NAV vurderer om du må betale tilbake ";
    private static final String TITTEL_VARSEL_TILBAKEBETALING = "Varsel tilbakebetaling ";

    private VarselbrevOverskrift() {
        //for static access
    }

    public static String finnOverskriftVarselbrev(String fagsaktypePåRiktigSpråk) {
        return OVERSKRIFT_VARSELBREV + fagsaktypePåRiktigSpråk;
    }


    //Tittel = navn på brevet som sendes med til Dokprod og dukker opp i dokumentlista i fpsak. Alltid bokmål.
    public static String finnTittelVarselbrev(String fagsaktypenavnBokmål) {
        return TITTEL_VARSEL_TILBAKEBETALING + fagsaktypenavnBokmål;
    }


}
