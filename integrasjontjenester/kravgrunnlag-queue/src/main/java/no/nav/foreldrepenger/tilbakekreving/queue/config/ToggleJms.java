package no.nav.foreldrepenger.tilbakekreving.queue.config;

import javax.enterprise.context.ApplicationScoped;

import no.nav.foreldrepenger.konfig.Cluster;
import no.nav.foreldrepenger.konfig.Environment;

@ApplicationScoped
public class ToggleJms implements no.nav.vedtak.felles.integrasjon.jms.ToggleJms {

    public static final String TOGGLE_JMS = "felles.jms";

    private static final Environment ENV = Environment.current();

    private final boolean enabled;

    public ToggleJms() {
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
