package no.nav.foreldrepenger.tilbakekreving.kravgrunnlag.queue.consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.felles.integrasjon.jms.JmsKonfig;

@ApplicationScoped
public class KravgrunnlagJmsConsumerKonfig {

    public static final String JNDI_QUEUE = "jms/QueueFptilbakeKravgrunnlag";

    private JmsKonfig jmsKonfig;

    KravgrunnlagJmsConsumerKonfig() {
        // CDI
    }

    @Inject
    public KravgrunnlagJmsConsumerKonfig(@KonfigVerdi("systembruker.username") String bruker,
                                         @KonfigVerdi("systembruker.password") String passord,
                                         @KonfigVerdi("mqGateway02.hostname") String host,
                                         @KonfigVerdi("mqGateway02.port") int port,
                                         @KonfigVerdi("mqGateway02.name") String managerName,
                                         @KonfigVerdi("mqGateway02.channel") String channel,
                                         @KonfigVerdi("fptilbake.kravgrunnlag.queuename") String queueName) {
        this.jmsKonfig = new JmsKonfig(host, port, managerName, channel, bruker, passord, queueName, null);
    }

    public JmsKonfig getJmsKonfig() {
        return jmsKonfig;
    }
}
