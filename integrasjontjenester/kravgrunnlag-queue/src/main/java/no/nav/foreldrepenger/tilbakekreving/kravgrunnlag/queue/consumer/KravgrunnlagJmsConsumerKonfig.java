package no.nav.foreldrepenger.tilbakekreving.kravgrunnlag.queue.consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.felles.integrasjon.jms.BaseJmsKonfig;

@Named("kravgrunnlagjmsconsumerkonfig")
@ApplicationScoped
public class KravgrunnlagJmsConsumerKonfig extends BaseJmsKonfig {

    public static final String JNDI_QUEUE = "jms/QueueFptilbakeKravgrunnlag";
    private static final String INN_QUEUE_PREFIX = "fptilbake_kravgrunnlag";

    private String mqBruker;
    private String mqPassord;

    private KravgrunnlagJmsConsumerKonfig() {
        super(INN_QUEUE_PREFIX);
    }

    @Inject
    public KravgrunnlagJmsConsumerKonfig(@KonfigVerdi("systembruker.username") String bruker,
                                         @KonfigVerdi("systembruker.password") String passord) {
        this();
        this.mqBruker = bruker;
        this.mqPassord = passord;
    }

    @Override
    public String getQueueManagerUsername() {
        return mqBruker;
    }

    @Override
    public String getQueueManagerPassword() {
        return mqPassord;
    }

}
