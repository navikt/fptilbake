package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.fagsystem.Fptilbake;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.K9tilbake;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.sikkerhet.abac.PdpRequestBuilder;

@ApplicationScoped
public class PdpRequestBuilderPruducer {

    private static final Logger logger = LoggerFactory.getLogger(PdpRequestBuilderPruducer.class);

    private PdpRequestBuilder pdpRequestBuilder;

    PdpRequestBuilderPruducer() {
    }

    @Inject
    public PdpRequestBuilderPruducer(@KonfigVerdi(value = "app.name") String applikasjon,
                                     @Any Instance<PdpRequestBuilder> pdpRequestBuilders) {
        switch (applikasjon) {
            case "fptilbake":
                logger.info("Bruker PdpRequestBuilder for fptilbake");
                pdpRequestBuilder = pdpRequestBuilders.select(new Fptilbake.FptilbakeAnnotationLiteral()).get();
                break;
            case "k9tilbake":
                logger.info("Bruker PdpRequestBuilder for k9");
                pdpRequestBuilder = pdpRequestBuilders.select(new K9tilbake.K9tilbakeAnnotationLiteral()).get();
                break;
            default:
                throw new IllegalStateException("app.name er satt til " + applikasjon + " som ikke er en støttet verdi");
        }
    }

    @Produces
    @ApplicationScoped
    public PdpRequestBuilder lagPdpRequestBuilder() {
        return pdpRequestBuilder;
    }
}
