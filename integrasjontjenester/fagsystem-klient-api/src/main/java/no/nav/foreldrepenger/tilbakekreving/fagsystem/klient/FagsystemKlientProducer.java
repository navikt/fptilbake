package no.nav.foreldrepenger.tilbakekreving.fagsystem.klient;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.fagsystem.Fptilbake;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.K9tilbake;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class FagsystemKlientProducer {

    private FagsystemKlient fagsystemKlient;

    FagsystemKlientProducer() {
    }

    @Inject
    public FagsystemKlientProducer(@KonfigVerdi(value = "app.name") String applikasjon, @Any Instance<FagsystemKlient> fagsystemklienter) {
        switch (applikasjon) {
            case "fptilbake" -> fagsystemKlient = fagsystemklienter.select(new Fptilbake.FptilbakeAnnotationLiteral()).get();
            case "k9-tilbake" -> fagsystemKlient = fagsystemklienter.select(new K9tilbake.K9tilbakeAnnotationLiteral()).get();
            default -> throw new IllegalStateException("app.name er satt til " + applikasjon + " som ikke er en støttet verdi");
        }
    }

    @Produces
    @ApplicationScoped
    public FagsystemKlient produce() {
        return fagsystemKlient;
    }


}
