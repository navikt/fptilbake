package no.nav.foreldrepenger.tilbakekreving.felles;

public interface KafkaIntegration {

    /**
     * Er integrasjonen i live (og ready).
     *
     * @return true / false
     */
    boolean isAlive();
}
