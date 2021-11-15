package no.nav.foreldrepenger.tilbakekreving.web.server.jetty;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;

public class JettyWebKonfigurasjon implements AppKonfigurasjon {
    private static final Environment ENV = Environment.current();
    private Integer serverPort;

    public JettyWebKonfigurasjon() {
    }

    public JettyWebKonfigurasjon(int serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public int getServerPort() {
        if (serverPort == null) {
            return AppKonfigurasjon.DEFAULT_SERVER_PORT;
        }
        return serverPort;
    }

    @Override
    public String getContextPath() {
        var appname = ApplicationName.hvilkenTilbake();
        return switch (appname) {
            case FPTILBAKE -> "/fptilbake";
            case K9TILBAKE -> "/k9/tilbake";
            default -> throw new IllegalArgumentException("Ikke-støttet applikasjonsnavn: " + appname);
        };
    }

    @Override
    public int getSslPort() {
        throw new IllegalStateException("SSL port should only be used locally");
    }
}
