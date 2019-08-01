package no.nav.foreldrepenger.tilbakekreving.kravgrunnlag.queue.consumer;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.integrasjon.jms.InternalQueueConsumer;
import no.nav.vedtak.felles.integrasjon.jms.JmsKonfig;
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
    public KravgrunnlagAsyncJmsConsumer(DefaultDatabaseOppePreconditionChecker preconditionChecker, @Named("kravgrunnlagjmsconsumerkonfig") JmsKonfig konfig, BeanManager beanManager) {
        super(konfig);
        this.preconditionChecker = preconditionChecker;
        this.beanManager = beanManager;
    }

    @Override
    public PreconditionChecker getPreconditionChecker() {
        return preconditionChecker;
    }

    @Override
    public void handle(Message message) throws JMSException {
        logger.info("Mottok en melding over MQ av type {}" , message.getClass().getName());
        if (message instanceof TextMessage) {
            håndterMelding((TextMessage) message);
        } else {
            FeilFactory.create(KravgrunnlagAsyncJmsConsumerFeil.class).ikkeStøttetMessage(message.getClass()).log(logger);
        }
    }

    private void håndterMelding(TextMessage message) throws JMSException {
        /**
         * håndterer ved å sende videre som Event, for å unngå sirkulær avhengighet
         */
        String meldingsinnhold = message.getText();
        KravgrunnlagMottattEvent event = new KravgrunnlagMottattEvent(meldingsinnhold);
        beanManager.fireEvent(event);
    }

    @Override
    @Resource(mappedName = KravgrunnlagJmsConsumerKonfig.JNDI_JMS_CONNECTION_FACTORY)
    protected void setConnectionFactory(ConnectionFactory connectionFactory) {
        super.setConnectionFactory(connectionFactory);
    }

    @Override
    @Resource(mappedName = KravgrunnlagJmsConsumerKonfig.JNDI_QUEUE)
    protected void setQueue(Queue queue) {
        super.setQueue(queue);
    }


    interface KravgrunnlagAsyncJmsConsumerFeil extends DeklarerteFeil {
        @TekniskFeil(feilkode = "FPT-832935", feilmelding = "Mottok på ikke støttet message av klasse %s. Kø-elementet ble ignorert", logLevel = LogLevel.WARN)
        Feil ikkeStøttetMessage(Class<? extends Message> klasse);
    }

}
