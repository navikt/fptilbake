package no.nav.foreldrepenger.tilbakekreving.web.server.jetty;

import no.nav.foreldrepenger.konfig.Environment;

public class JettyFpDevServer extends JettyServer {

    private static final Environment ENV = Environment.current();

    public static void main(String[] args) throws Exception {
        jettyServer(args).bootStrap();
    }

    static JettyFpDevServer jettyServer(String[] args) {
        if (args.length > 0) {
            return new JettyFpDevServer(Integer.parseUnsignedInt(args[0]));
        }
        return new JettyFpDevServer(ENV.getProperty("server.port", Integer.class, 8030));
    }

    private JettyFpDevServer(int serverPort) {
        super(serverPort);
    }
}
