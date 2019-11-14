package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel;

public class VarselbrevOverskrift {

    private static final String OVERSKRIFT_VARSELBREV = "NAV vurderer om du må betale tilbake ";
    private static final String TITTEL_VARSEL_TILBAKEBETALING = "Varsel tilbakebetaling ";

    private static final String OVERSKRIFT_KORRIGERT_VARSELBREV = "Korrigert varsel om feilutbetalte ";
    private static final String OVERSKRIFT_KORRIGERT_VARSELBREV_ENGANGSSTØNAD = "Korrigert varsel om feilutbetalt ";
    private static final String TITTEL_KORRIGERT_VARSEL_TILBAKEBETALING = "Korrigert Varsel tilbakebetaling ";

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

    public static String finnOverskriftKorrigertVarselbrev(String fagsaktypePåRiktigSpråk) {
        return OVERSKRIFT_KORRIGERT_VARSELBREV + fagsaktypePåRiktigSpråk;
    }

    public static String finnOverskriftKorrigertVarselbrevEnngangsstønad(String fagsaktypePåRiktigSpråk) {
        return OVERSKRIFT_KORRIGERT_VARSELBREV_ENGANGSSTØNAD + fagsaktypePåRiktigSpråk;
    }

    public static String finnTittelKorrigertVarselbrev(String fagsaktypenavnBokmål) {
        return TITTEL_KORRIGERT_VARSEL_TILBAKEBETALING + fagsaktypenavnBokmål;
    }

}
