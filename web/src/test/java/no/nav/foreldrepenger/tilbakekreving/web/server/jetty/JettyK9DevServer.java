package no.nav.foreldrepenger.tilbakekreving.web.server.jetty;

import no.nav.foreldrepenger.konfig.Environment;

public class JettyK9DevServer extends JettyServer {

    private static final Environment ENV = Environment.current();

    public static void main(String[] args) throws Exception {
        jettyServer(args).bootStrap();
    }

    static JettyK9DevServer jettyServer(String[] args) {
        if (args.length > 0) {
            return new JettyK9DevServer(Integer.parseUnsignedInt(args[0]));
        }
        return new JettyK9DevServer(ENV.getProperty("server.port", Integer.class, 8030));
    }

    private JettyK9DevServer(int serverPort) {
        super(serverPort);
    }
}
