package no.nav.foreldrepenger.tilbakekreving.kravgrunnlag.queue.consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import no.nav.vedtak.felles.integrasjon.jms.BaseJmsKonfig;

@Named("kravgrunnlagjmsconsumerkonfig")
@ApplicationScoped
public class KravgrunnlagJmsConsumerKonfig extends BaseJmsKonfig {

    public static final String JNDI_QUEUE = "jms/QueueFptilbakeKravgrunnlag";

    private static final String INN_QUEUE_PREFIX = "fptilbake_kravgrunnlag";


    public KravgrunnlagJmsConsumerKonfig() {
        super(INN_QUEUE_PREFIX);
    }

    @Override
    public String getQueueManagerUsername() {
        return "srvappserver"; // TODO - hent fra konfig når ny MQ-konfig innføres i august/september
    }

    @Override
    public String getQueueManagerPassword() {
        return null; // TODO - hent fra vault e.l. når ny MQ-konfig innføres i august/september
    }

}
