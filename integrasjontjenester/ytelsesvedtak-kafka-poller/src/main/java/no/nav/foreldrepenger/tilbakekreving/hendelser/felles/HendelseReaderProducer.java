package no.nav.foreldrepenger.tilbakekreving.hendelser.felles;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.Fptilbake;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.K9tilbake;

@ApplicationScoped
public class HendelseReaderProducer {

    private HendelseReader hendelseReader;

    HendelseReaderProducer(){
        // for CDI
    }

    @Inject
    public HendelseReaderProducer(@KonfigVerdi(value = "app.name") String applikasjon, @Any Instance<HendelseReader> hendelseReaders) {
        switch (applikasjon) {
            case "fptilbake" -> hendelseReader = hendelseReaders.select(new Fptilbake.FptilbakeAnnotationLiteral()).get();
            case "k9-tilbake" -> hendelseReader = hendelseReaders.select(new K9tilbake.K9tilbakeAnnotationLiteral()).get();
            default -> throw new IllegalStateException("app.name er satt til " + applikasjon + " som ikke er en støttet verdi");
        }
    }

    @Produces
    @ApplicationScoped
    public HendelseReader produce() {
        return hendelseReader;
    }
}
