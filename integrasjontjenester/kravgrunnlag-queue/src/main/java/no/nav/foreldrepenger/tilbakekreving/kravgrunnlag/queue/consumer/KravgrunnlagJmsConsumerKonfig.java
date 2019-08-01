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
}
