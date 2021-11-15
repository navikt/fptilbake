package no.nav.foreldrepenger.tilbakekreving.fagsystem;

import javax.ws.rs.core.Application;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;


public final class ApplicationName extends Application {

    private static final Environment ENV = Environment.current();
    private static Fagsystem CURRENT;

    private static final String APPLIKASJON_NAVN_K9_TILBAKE = "k9-tilbake";
    private static final String APPLIKASJON_NAVN_FPTILBAKE = "fptilbake";

    public static Fagsystem hvilkenTilbake() {
        if (CURRENT == null) {
            CURRENT = getCurrentApp();
        }
        return CURRENT;
    }

    private static Fagsystem getCurrentApp() {
        String applikasjon= ENV.getProperty("app.name");
        return switch (applikasjon) {
            case APPLIKASJON_NAVN_FPTILBAKE -> Fagsystem.FPTILBAKE;
            case APPLIKASJON_NAVN_K9_TILBAKE -> Fagsystem.K9TILBAKE;
            default -> throw new IllegalStateException("app.name er satt til " + applikasjon + " som ikke er en støttet verdi");
        };
    }
}
