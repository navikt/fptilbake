package no.nav.foreldrepenger.tilbakekreving.fagsystem.klient;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.Fptilbake;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.K9tilbake;

@ApplicationScoped
public class FagsystemKlientProducer {

    private FagsystemKlient fagsystemKlient;

    FagsystemKlientProducer() {
    }

    @Inject
    public FagsystemKlientProducer(@Any Instance<FagsystemKlient> fagsystemklienter) {
        var applikasjon = ApplicationName.hvilkenTilbake();
        fagsystemKlient = switch (applikasjon) {
            case FPTILBAKE -> fagsystemklienter.select(new Fptilbake.FptilbakeAnnotationLiteral()).get();
            case K9TILBAKE -> fagsystemklienter.select(new K9tilbake.K9tilbakeAnnotationLiteral()).get();
            default -> throw new IllegalStateException("applikasjonsnavn er satt til " + applikasjon + " som ikke er en støttet verdi");
        };
    }

    @Produces
    @ApplicationScoped
    public FagsystemKlient produce() {
        return fagsystemKlient;
    }


}
