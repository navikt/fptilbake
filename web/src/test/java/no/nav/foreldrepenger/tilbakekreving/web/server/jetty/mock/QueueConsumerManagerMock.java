package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.mock;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Specializes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.integrasjon.jms.QueueConsumer;
import no.nav.vedtak.felles.integrasjon.jms.QueueConsumerManagerImpl;

@Specializes
public class QueueConsumerManagerMock extends QueueConsumerManagerImpl {

    private static final Logger logger = LoggerFactory.getLogger(QueueConsumerManagerMock.class);

    public QueueConsumerManagerMock() {
    }

    @Override
    public void initConsumers(Instance<QueueConsumer> consumersInstance) {
        logger.info("ignorerer queue-consumers");
    }

    @Override
    public synchronized void start() {

    }

    @Override
    public synchronized void stop() {

    }
}
