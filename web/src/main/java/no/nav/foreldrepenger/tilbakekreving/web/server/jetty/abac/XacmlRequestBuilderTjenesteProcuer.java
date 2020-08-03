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
import no.nav.vedtak.sikkerhet.pdp.XacmlRequestBuilderTjeneste;

@ApplicationScoped
public class XacmlRequestBuilderTjenesteProcuer {

    private static final Logger logger = LoggerFactory.getLogger(XacmlRequestBuilderTjenesteProcuer.class);

    private XacmlRequestBuilderTjeneste xacmlRequestBuilderTjeneste;

    XacmlRequestBuilderTjenesteProcuer() {
    }

    @Inject
    public XacmlRequestBuilderTjenesteProcuer(@KonfigVerdi(value = "app.name") String applikasjon,
                                              @Any Instance<XacmlRequestBuilderTjeneste> xacmlRequestBuilderTjenester) {
        switch (applikasjon) {
            case "fptilbake":
                logger.info("Bruker XacmlRequestBuilderTjeneste for fptilbake");
                xacmlRequestBuilderTjeneste = xacmlRequestBuilderTjenester.select(new Fptilbake.FptilbakeAnnotationLiteral()).get();
                break;
            case "k9tilbake":
                logger.info("Bruker XacmlRequestBuilderTjeneste for k9");
                xacmlRequestBuilderTjeneste = xacmlRequestBuilderTjenester.select(new K9tilbake.K9tilbakeAnnotationLiteral()).get();
                break;
            default:
                throw new IllegalStateException("app.name er satt til " + applikasjon + " som ikke er en støttet verdi");
        }
    }

    @Produces
    @ApplicationScoped
    public XacmlRequestBuilderTjeneste lagPdpRequestBuilder() {
        return xacmlRequestBuilderTjeneste;
    }
}
