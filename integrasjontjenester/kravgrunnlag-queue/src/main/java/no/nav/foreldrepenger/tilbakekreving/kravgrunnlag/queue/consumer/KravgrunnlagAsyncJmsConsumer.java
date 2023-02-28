package no.nav.foreldrepenger.tilbakekreving.kravgrunnlag.queue.consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import no.nav.foreldrepenger.felles.jms.QueueConsumer;

import no.nav.foreldrepenger.felles.jms.precond.PreconditionChecker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import no.nav.vedtak.log.metrics.Controllable;

@ApplicationScoped
public class KravgrunnlagAsyncJmsConsumer extends QueueConsumer implements Controllable {

    private static final Logger LOGGER = LoggerFactory.getLogger(KravgrunnlagAsyncJmsConsumer.class);

    private DatabasePreconditionChecker preconditionChecker;
    private BeanManager beanManager;

    KravgrunnlagAsyncJmsConsumer() {
        // CDI
    }

    @Inject
    public KravgrunnlagAsyncJmsConsumer(DatabasePreconditionChecker preconditionChecker,
                                        KravgrunnlagJmsConsumerKonfig konfig,
                                        BeanManager beanManager) {
        super(konfig.getJmsKonfig());
        super.setConnectionFactory(konfig.getMqConnectionFactory());
        super.setQueue(konfig.getMqQueue());
        super.setToggleJms(new FellesJmsToggle());
        super.setMdcHandler(new QueueMdcLogHandler());
        this.preconditionChecker = preconditionChecker;
        this.beanManager = beanManager;
    }

    @Override
    public PreconditionChecker getPreconditionChecker() {
        return preconditionChecker;
    }

    @Override
    public void handle(Message message) throws JMSException {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Mottok en melding over MQ av type {}", message.getClass().getName());
        }

        if (message instanceof TextMessage tm) {
            håndterMelding(tm.getText());
        } else if (LOGGER.isWarnEnabled()) {
            LOGGER.warn("FPT-832935: Mottok en ikke støttet melding av klasse {}. Kø-elementet ble ignorert.", message.getClass().getName());
        }
    }

    /**
     * håndterer ved å sende videre som Event, for å unngå sirkulær avhengighet
     */
    private void håndterMelding(String message) {
        XmlMottattEvent event = new XmlMottattEvent(message);
        beanManager.fireEvent(event);
    }

    @Override
    public void start() {
        if (!isDisabled()) {
            LOGGER.debug("Starter {}", KravgrunnlagAsyncJmsConsumer.class.getSimpleName());
            super.start();
            LOGGER.info("Startet: {}", KravgrunnlagAsyncJmsConsumer.class.getSimpleName());
        }
    }

    @Override
    public void stop() {
        if (!isDisabled()) {
            LOGGER.debug("Stoping {}", KravgrunnlagAsyncJmsConsumer.class.getSimpleName());
            super.stop();
            LOGGER.info("Stoppet: {}", KravgrunnlagAsyncJmsConsumer.class.getSimpleName());
        }
    }
}
