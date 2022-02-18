package no.nav.foreldrepenger.tilbakekreving.fagsystem;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;


public final class ApplicationName {

    private static final Environment ENV = Environment.current();

    private static final String APPLIKASJON_NAVN_K9_TILBAKE = "k9-tilbake";
    private static final String APPLIKASJON_NAVN_FPTILBAKE = "fptilbake";

    private static Fagsystem CURRENT_APPLIKASJON;
    private static String CURRENT_APPLIKASJON_NAVN;

    public static Fagsystem hvilkenTilbake() {
        if (CURRENT_APPLIKASJON == null) {
            setCurrentApp();
        }
        return CURRENT_APPLIKASJON;
    }

    public static String hvilkenTilbakeAppName() {
        if (CURRENT_APPLIKASJON == null) {
            setCurrentApp();
        }
        return CURRENT_APPLIKASJON_NAVN;
    }

    private static void setCurrentApp() {
        CURRENT_APPLIKASJON_NAVN = ENV.getRequiredProperty("app.name");
        CURRENT_APPLIKASJON = switch (CURRENT_APPLIKASJON_NAVN) {
            case APPLIKASJON_NAVN_FPTILBAKE -> Fagsystem.FPTILBAKE;
            case APPLIKASJON_NAVN_K9_TILBAKE -> Fagsystem.K9TILBAKE;
            default -> throw new IllegalStateException("applikasjonsnavn er satt til " + CURRENT_APPLIKASJON_NAVN + " som ikke er en støttet verdi");
        };
    }
}
