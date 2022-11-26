package no.nav.foreldrepenger.tilbakekreving.fplos.klient.task;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.foreldrepenger.tilbakekreving.fplos.klient.producer.LosKafkaProducerAiven;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

/**
 * På Aiven i nais-plattformen mister consumer offset hvis det går over en uke mellom hver gang det commites offsets. Det igjen begyr
 * at offset vil bli mistet hvis det går en uke uten trafikk. På tilbakekreving kan det skje, spesielt ved ferieavvikling.
 * <p>
 * Sender derfor her periodisk meldinger som leses, men kan av consumeren. Bare slik at consumeren for oppdatert offset.
 * <p>
 * Se https://doc.nais.io/persistence/kafka/offsets/
 */
@ApplicationScoped
@ProsessTask(value = "kafka.heartbeat.los", cronExpression = "0 0/15 * * * * " /* TODO kan reduseres til 1 om dagen, har her 1 hvert kvarter for å slippe å vente når vi tester */)
public class LosHeartbeatTask implements ProsessTaskHandler {

    private LosKafkaProducerAiven kafkaProducer;
    private boolean brukAiven;

    public LosHeartbeatTask() {
        //for CDI proxy
    }

    @Inject
    public LosHeartbeatTask(LosKafkaProducerAiven kafkaProducer,
                            @KonfigVerdi(value = "toggle.aiven.los", defaultVerdi = "false") boolean brukAiven) {
        this.kafkaProducer = kafkaProducer;
        this.brukAiven = brukAiven;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        if (ApplicationName.hvilkenTilbake() == Fagsystem.K9TILBAKE && brukAiven) {
            kafkaProducer.sendHeartbeat();
        }
    }
}
