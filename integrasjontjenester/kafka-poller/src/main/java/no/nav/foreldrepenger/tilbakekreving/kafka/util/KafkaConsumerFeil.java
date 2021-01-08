package no.nav.foreldrepenger.tilbakekreving.kafka.util;

import org.apache.kafka.clients.consumer.CommitFailedException;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface KafkaConsumerFeil extends DeklarerteFeil {

    KafkaConsumerFeil FACTORY = FeilFactory.create(KafkaConsumerFeil.class);

    @TekniskFeil(feilkode = "FPT-051", feilmelding = "Kan ikke commit offset. Meldinger kan bli lest flere ganger.", logLevel = LogLevel.WARN)
    Feil kunneIkkeCommitOffset(CommitFailedException cause);
}
