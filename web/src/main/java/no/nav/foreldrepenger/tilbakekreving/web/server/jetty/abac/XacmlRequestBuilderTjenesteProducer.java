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
import no.nav.vedtak.sikkerhet.pdp.XacmlRequestBuilderTjeneste;

@ApplicationScoped
public class XacmlRequestBuilderTjenesteProducer {

    private static final Logger logger = LoggerFactory.getLogger(XacmlRequestBuilderTjenesteProducer.class);

    private XacmlRequestBuilderTjeneste xacmlRequestBuilderTjeneste;

    XacmlRequestBuilderTjenesteProducer() {
    }

    @Inject
    public XacmlRequestBuilderTjenesteProducer(@Any Instance<XacmlRequestBuilderTjeneste> xacmlRequestBuilderTjenester) {
        var applikasjon = ApplicationName.hvilkenTilbake();
        switch (applikasjon) {
            case FPTILBAKE -> {
                logger.info("Bruker XacmlRequestBuilderTjeneste for fptilbake");
                xacmlRequestBuilderTjeneste = xacmlRequestBuilderTjenester.select(new Fptilbake.FptilbakeAnnotationLiteral()).get();
            }
            case K9TILBAKE -> {
                logger.info("Bruker XacmlRequestBuilderTjeneste for k9");
                xacmlRequestBuilderTjeneste = xacmlRequestBuilderTjenester.select(new K9tilbake.K9tilbakeAnnotationLiteral()).get();
            }
            default -> throw new IllegalStateException("app.name er satt til " + applikasjon + " som ikke er en støttet verdi");
        }
    }

    @Produces
    @ApplicationScoped
    public XacmlRequestBuilderTjeneste lagPdpRequestBuilder() {
        return xacmlRequestBuilderTjeneste;
    }
}
