package no.nav.foreldrepenger.tilbakekreving.kafka.util;

import org.apache.kafka.clients.consumer.CommitFailedException;

import no.nav.vedtak.exception.TekniskException;

public class KafkaConsumerFeil  {

    public static TekniskException kunneIkkeCommitOffset(CommitFailedException cause) {
        return new TekniskException("FPT-051", "Kan ikke commit offset. Meldinger kan bli lest flere ganger.", cause);
    }
}
