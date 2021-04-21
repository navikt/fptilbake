package no.nav.foreldrepenger.tilbakekreving.web.server.jetty;

import no.nav.vedtak.util.env.Environment;

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
        String appname = ENV.getProperty("app.name");
        switch (appname) {
            case "fptilbake":
                return "/fptilbake";
            case "k9-tilbake":
                return "/k9/tilbake";
            default:
                throw new IllegalArgumentException("Ikke-støttet applikasjonsnavn: " + appname);
        }
    }

    @Override
    public int getSslPort() {
        throw new IllegalStateException("SSL port should only be used locally");
    }
}
