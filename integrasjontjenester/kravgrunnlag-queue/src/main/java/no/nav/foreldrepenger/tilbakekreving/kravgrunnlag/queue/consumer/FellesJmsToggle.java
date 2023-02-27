package no.nav.foreldrepenger.tilbakekreving.kravgrunnlag.queue.consumer;

import no.nav.foreldrepenger.konfig.Cluster;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.felles.integrasjon.jms.ToggleJms;

class FellesJmsToggle implements ToggleJms {

    public static final String TOGGLE_JMS = "felles.jms";

    private static final Environment ENV = Environment.current();

    private final boolean enabled;

    public FellesJmsToggle() {
        boolean clusterDefault = !Cluster.LOCAL.equals(ENV.getCluster());
        String jmsEnabled = ENV.getProperty(TOGGLE_JMS, Boolean.toString(clusterDefault));
        this.enabled = Boolean.parseBoolean(jmsEnabled);
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isDisabled() {
        return !isEnabled();
    }
}
