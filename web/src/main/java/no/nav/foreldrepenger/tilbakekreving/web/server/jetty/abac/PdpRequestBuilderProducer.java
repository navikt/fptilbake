package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.Fptilbake;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.K9tilbake;
import no.nav.vedtak.sikkerhet.abac.PdpRequestBuilder;

@ApplicationScoped
public class PdpRequestBuilderProducer {

    private static final Logger LOG = LoggerFactory.getLogger(PdpRequestBuilderProducer.class);

    private PdpRequestBuilder pdpRequestBuilder;

    PdpRequestBuilderProducer() {
    }

    @Inject
    public PdpRequestBuilderProducer(@Any Instance<PdpRequestBuilder> pdpRequestBuilders) {
        var applikasjon = ApplicationName.hvilkenTilbake();
        switch (applikasjon) {
            case FPTILBAKE -> {
                LOG.info("Bruker PdpRequestBuilder for fptilbake");
                pdpRequestBuilder = pdpRequestBuilders.select(new Fptilbake.FptilbakeAnnotationLiteral()).get();
            }
            case K9TILBAKE -> {
                LOG.info("Bruker PdpRequestBuilder for k9");
                pdpRequestBuilder = pdpRequestBuilders.select(new K9tilbake.K9tilbakeAnnotationLiteral()).get();
            }
            default -> throw new IllegalStateException("applikasjonsnavn er satt til " + applikasjon + " som ikke er en støttet verdi");
        }
    }

    @Produces
    @ApplicationScoped
    public PdpRequestBuilder lagPdpRequestBuilder() {
        return pdpRequestBuilder;
    }
}
