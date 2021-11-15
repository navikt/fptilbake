package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.Fptilbake;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.K9tilbake;
import no.nav.vedtak.sikkerhet.abac.PdpRequestBuilder;

@ApplicationScoped
public class PdpRequestBuilderProducer {

    private static final Logger logger = LoggerFactory.getLogger(PdpRequestBuilderProducer.class);

    private PdpRequestBuilder pdpRequestBuilder;

    PdpRequestBuilderProducer() {
    }

    @Inject
    public PdpRequestBuilderProducer(@Any Instance<PdpRequestBuilder> pdpRequestBuilders) {
        var applikasjon = ApplicationName.hvilkenTilbake();
        switch (applikasjon) {
            case FPTILBAKE -> {
                logger.info("Bruker PdpRequestBuilder for fptilbake");
                pdpRequestBuilder = pdpRequestBuilders.select(new Fptilbake.FptilbakeAnnotationLiteral()).get();
            }
            case K9TILBAKE -> {
                logger.info("Bruker PdpRequestBuilder for k9");
                pdpRequestBuilder = pdpRequestBuilders.select(new K9tilbake.K9tilbakeAnnotationLiteral()).get();
            }
            default -> throw new IllegalStateException("app.name er satt til " + applikasjon + " som ikke er en støttet verdi");
        }
    }

    @Produces
    @ApplicationScoped
    public PdpRequestBuilder lagPdpRequestBuilder() {
        return pdpRequestBuilder;
    }
}
