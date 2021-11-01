package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner;

public class FellesTransisjoner {

    public static final String FREMHOPP_PREFIX = "fremhopp-til-";
    private static final String TILBAKEFØR_PREFIX = "tilbakeført-til-";

    public static final TransisjonIdentifikator UTFØRT = TransisjonIdentifikator.forId("utført");
    public static final TransisjonIdentifikator STARTET = TransisjonIdentifikator.forId("startet");
    public static final TransisjonIdentifikator HENLAGT = TransisjonIdentifikator.forId("henlagt");
    public static final TransisjonIdentifikator SETT_PÅ_VENT = TransisjonIdentifikator.forId("sett-på-vent");
    public static final TransisjonIdentifikator TILBAKEFØRT_TIL_AKSJONSPUNKT = TransisjonIdentifikator.forId(TILBAKEFØR_PREFIX + "aksjonspunkt");
    public static final TransisjonIdentifikator FREMHOPP_TIL_FATTE_VEDTAK = TransisjonIdentifikator.forId(FREMHOPP_PREFIX + "fatte-vedtak");
    public static final TransisjonIdentifikator FREMHOPP_TIL_FORESLÅ_VEDTAK = TransisjonIdentifikator.forId(FREMHOPP_PREFIX + "foreslå-vedtak");
    public static final TransisjonIdentifikator FREMHOPP_TIL_IVERKSETT_VEDTAK = TransisjonIdentifikator.forId(FREMHOPP_PREFIX + "iverksett-vedtak");

    private FellesTransisjoner() {
        //hindrer instansiering
    }

    public static boolean erFremhoppTransisjon(TransisjonIdentifikator transisjonIdentifikator) {
        return transisjonIdentifikator.getId().startsWith(FREMHOPP_PREFIX);
    }

}
