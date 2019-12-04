package no.nav.foreldrepenger.tilbakekreving.web.server.jetty;

public class JettyDevKonfigurasjon extends JettyWebKonfigurasjon {
    private static final int SSL_SERVER_PORT = 8444;
    private static int DEFAULT_DEV_SERVER_PORT = 8030;

    JettyDevKonfigurasjon(){
        super(DEFAULT_DEV_SERVER_PORT);
    }

    @Override
    public int getSslPort() {
        return SSL_SERVER_PORT;
    }
}
