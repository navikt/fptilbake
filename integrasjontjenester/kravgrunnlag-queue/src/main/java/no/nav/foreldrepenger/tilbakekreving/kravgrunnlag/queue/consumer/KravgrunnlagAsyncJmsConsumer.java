package no.nav.foreldrepenger.tilbakekreving.kravgrunnlag.queue.consumer;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.integrasjon.jms.InternalQueueConsumer;
import no.nav.vedtak.felles.integrasjon.jms.precond.DefaultDatabaseOppePreconditionChecker;
import no.nav.vedtak.felles.integrasjon.jms.precond.PreconditionChecker;

@ApplicationScoped
public class KravgrunnlagAsyncJmsConsumer extends InternalQueueConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KravgrunnlagAsyncJmsConsumer.class);

    private DefaultDatabaseOppePreconditionChecker preconditionChecker;
    private BeanManager beanManager;

    public KravgrunnlagAsyncJmsConsumer() {
        // CDI
    }

    @Inject
    public KravgrunnlagAsyncJmsConsumer(DefaultDatabaseOppePreconditionChecker preconditionChecker,
                                        KravgrunnlagJmsConsumerKonfig konfig,
                                        BeanManager beanManager) {
        super(konfig.getJmsKonfig());
        super.setConnectionFactory(konfig.getMqConnectionFactory());
        super.setQueue(konfig.getMqQueue());
        this.preconditionChecker = preconditionChecker;
        this.beanManager = beanManager;
    }

    @Override
    public PreconditionChecker getPreconditionChecker() {
        return preconditionChecker;
    }

    @Override
    public void handle(Message message) throws JMSException {
        logger.info("Mottok en melding over MQ av type {}", message.getClass().getName());
        if (message instanceof TextMessage) {
            håndterMelding((TextMessage) message);
        } else {
            logger.warn(String.format("FPT-832935: Mottok på ikke støttet message av klasse %s. Kø-elementet ble ignorert", message.getClass().getName()));
        }
    }

    private void håndterMelding(TextMessage message) throws JMSException {
        /**
         * håndterer ved å sende videre som Event, for å unngå sirkulær avhengighet
         */
        String meldingsinnhold = message.getText();
        XmlMottattEvent event = new XmlMottattEvent(meldingsinnhold);
        beanManager.fireEvent(event);
    }
}
