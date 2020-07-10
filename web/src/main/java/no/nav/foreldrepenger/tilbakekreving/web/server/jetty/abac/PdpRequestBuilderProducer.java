package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.Produces;

import no.nav.foreldrepenger.tilbakekreving.fagsystem.Fptilbake;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.K9tilbake;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.sikkerhet.abac.PdpRequestBuilder;

@ApplicationScoped
@Alternative
@Priority(2)
public class PdpRequestBuilderProducer {

    private PdpRequestBuilder pdpRequestBuilder;

    PdpRequestBuilderProducer() {
    }

    @Inject
    public PdpRequestBuilderProducer(@KonfigVerdi(value = "app.name") String applikasjon,
                                     @Any Instance<PdpRequestBuilder> pdpRequestBuilders) {
        System.out.println("Blir denne i det heile tatt kalla?");
        switch (applikasjon) {
            case "fptilbake":
                System.out.println("Bruker PdpRequestBuilder for fptilbake");
                pdpRequestBuilder = pdpRequestBuilders.select(new Fptilbake.FptilbakeAnnotationLiteral()).get();
                break;
            case "k9tilbake":
                System.out.println("Bruker PdpRequestBuilder for k9");
                pdpRequestBuilder = pdpRequestBuilders.select(new K9tilbake.K9tilbakeAnnotationLiteral()).get();
                break;
            default:
                throw new IllegalStateException("app.name er satt til " + applikasjon + " som ikke er en støttet verdi");
        }
    }

    @Produces
    @ApplicationScoped
    public PdpRequestBuilder produce() {
        return pdpRequestBuilder;
    }
}
