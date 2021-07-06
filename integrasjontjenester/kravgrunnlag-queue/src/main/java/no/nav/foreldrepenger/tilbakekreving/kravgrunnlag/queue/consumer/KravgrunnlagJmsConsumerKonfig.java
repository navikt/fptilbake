package no.nav.foreldrepenger.tilbakekreving.kravgrunnlag.queue.consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.felles.integrasjon.jms.BaseJmsKonfig;

@Named("kravgrunnlagjmsconsumerkonfig")
@ApplicationScoped
public class KravgrunnlagJmsConsumerKonfig extends BaseJmsKonfig {

    public static final String JNDI_QUEUE = "jms/QueueFptilbakeKravgrunnlag";
    private static final String INN_QUEUE_PREFIX = "fptilbake_kravgrunnlag";
    private static final String FPTILBAKE = "fptilbake";

    private String mqBruker;
    private String mqPassord;
    private String appName;

    private KravgrunnlagJmsConsumerKonfig() {
        super(INN_QUEUE_PREFIX);
    }

    @Inject
    public KravgrunnlagJmsConsumerKonfig(@KonfigVerdi("systembruker.username") String bruker,
                                         @KonfigVerdi("systembruker.password") String passord,
                                         @KonfigVerdi("app.name") String appName) {
        this();
        this.mqBruker = bruker;
        this.mqPassord = passord;
        this.appName = appName;
    }

    @Override
    public String getQueueManagerUsername() {
        return FPTILBAKE.equals(appName) ? mqBruker : "srvappserver";
    }

    @Override
    public String getQueueManagerPassword() {
        return FPTILBAKE.equals(appName) ? mqPassord : null;
    }

}
