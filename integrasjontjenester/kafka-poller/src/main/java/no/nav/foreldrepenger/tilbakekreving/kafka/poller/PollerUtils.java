package no.nav.foreldrepenger.tilbakekreving.kafka.poller;

import java.util.concurrent.ThreadFactory;

class PollerUtils {

    private PollerUtils() {
        // skjul default constructor
    }

    static class NamedThreadFactory implements ThreadFactory {

        private final String name;

        NamedThreadFactory(String name) {
            this.name = name;
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, name);
        }
    }
}
