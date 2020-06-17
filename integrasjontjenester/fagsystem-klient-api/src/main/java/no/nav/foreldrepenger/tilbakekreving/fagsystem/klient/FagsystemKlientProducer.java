package no.nav.foreldrepenger.tilbakekreving.fagsystem.klient;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.fagsystem.Fptilbake;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class FagsystemKlientProducer {

    private FagsystemKlient fagsystemKlient;

    FagsystemKlientProducer() {
    }

    @Inject
    public FagsystemKlientProducer(@KonfigVerdi(value = "app.name") String applikasjon,
                                   @Fptilbake FagsystemKlient fpsakKlient) {
        if ("fptilbake".equalsIgnoreCase(applikasjon)) {
            fagsystemKlient = fpsakKlient;
// TODO: Korleis støtte konfigurasjon for både fptilbake og k9-tilbake?
//        } else if ("k9tilbake".equals(applikasjon)) {
//            fagsystemKlient = k9sakKlient;
        } else {
            throw new IllegalStateException("app.name er satt til " + applikasjon + " som ikke er en støttet verdi");
        }
    }

    @Produces
    @ApplicationScoped
    public FagsystemKlient produce() {
        return fagsystemKlient;
    }
}
