package no.nav.foreldrepenger.tilbakekreving.web.app.selftest.checks;

import jakarta.jms.JMSException;
import jakarta.jms.JMSRuntimeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.integrasjon.jms.QueueSelftest;

public abstract class QueueHealthCheck {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseHealthCheck.class);

    private QueueSelftest client;

    protected QueueHealthCheck() {
        // for CDI proxy
    }

    protected QueueHealthCheck(QueueSelftest client) {
        this.client = client;
    }

    protected String getDescription() {
        return "Test av meldingskø for " + getDescriptionSuffix();
    }

    protected abstract String getDescriptionSuffix();

    protected String getEndpoint() {
        String endpoint;
        try {
            endpoint = client.getConnectionEndpoint();
        } catch (Exception e) { // NOSONAR
            endpoint = "Uventet feil: " + e.getMessage();
        }
        return endpoint;
    }

    public boolean isOk() {
        try {
            client.testConnection();
        } catch (JMSRuntimeException | JMSException e) { // NOSONAR
            LOG.warn("Feil ved meldingskø helsesjekk {}", getDescriptionSuffix());
        }

        return true;
    }
}
