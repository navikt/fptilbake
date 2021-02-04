package no.nav.foreldrepenger.tilbakekreving.web.server.jetty;

public class JettyWebKonfigurasjon implements AppKonfigurasjon {
    private static final String SWAGGER_HASH = "sha256-w6DoSiqz8+6cP13xAZftmJAdUupO32ZdbQZhwOvWf+U=";

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
        String appname = System.getProperty("application.name");
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

    @Override
    public String getSwaggerHash() {
        return SWAGGER_HASH;
    }
}
