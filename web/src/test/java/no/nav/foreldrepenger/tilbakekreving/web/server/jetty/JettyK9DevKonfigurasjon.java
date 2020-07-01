package no.nav.foreldrepenger.tilbakekreving.web.server.jetty;

public class JettyK9DevKonfigurasjon extends JettyWebKonfigurasjon {
    private static final int SSL_SERVER_PORT = 8445;
    private static int DEFAULT_DEV_SERVER_PORT = 8031;

    JettyK9DevKonfigurasjon(){
        super(DEFAULT_DEV_SERVER_PORT);
    }

    @Override
    public int getSslPort() {
        return SSL_SERVER_PORT;
    }
}
