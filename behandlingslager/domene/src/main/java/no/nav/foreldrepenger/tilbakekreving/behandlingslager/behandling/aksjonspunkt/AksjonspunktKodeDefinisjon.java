package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt;

/**
 * Konstanter for aksjonspunkt for å enklere kunne referere til dem i eksisterende logikk.
 */
public class AksjonspunktKodeDefinisjon {

    // Manuelle
    public static final String VURDER_TILBAKEKREVING = "5002";
    public static final String VURDER_FORELDELSE = "5003";
    public static final String FORESLÅ_VEDTAK = "5004";
    public static final String FATTE_VEDTAK = "5005";
    public static final String AVKLAR_VERGE = "5030";
    public static final String AVKLART_FAKTA_FEILUTBETALING = "7003";

    // Autopunkt
    public static final String VENT_PÅ_BRUKERTILBAKEMELDING = "7001";
    public static final String VENT_PÅ_TILBAKEKREVINGSGRUNNLAG = "7002";


    // Fiktivt aksjonspunkt - brukes for å sende data til fplos når behandling venter på grunnlaget etter fristen
    public static final String VURDER_HENLEGGELSE_MANGLER_KRAVGRUNNLAG = "8001";

    // Andre koder
    public static final boolean TOTRINN = true;
    public static final boolean ENTRINN = false;
    public static final boolean TILBAKE = true;
    public static final boolean FORBLI = false;
}
