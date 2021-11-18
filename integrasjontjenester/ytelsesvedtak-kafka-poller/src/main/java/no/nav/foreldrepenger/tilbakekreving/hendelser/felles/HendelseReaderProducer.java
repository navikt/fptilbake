package no.nav.foreldrepenger.tilbakekreving.hendelser.felles;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.Fptilbake;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.K9tilbake;

@ApplicationScoped
public class HendelseReaderProducer {

    private HendelseReader hendelseReader;

    HendelseReaderProducer(){
        // for CDI
    }

    @Inject
    public HendelseReaderProducer(@Any Instance<HendelseReader> hendelseReaders) {
        var applikasjon = ApplicationName.hvilkenTilbake();
        hendelseReader = switch (applikasjon) {
            case FPTILBAKE -> hendelseReaders.select(new Fptilbake.FptilbakeAnnotationLiteral()).get();
            case K9TILBAKE -> hendelseReaders.select(new K9tilbake.K9tilbakeAnnotationLiteral()).get();
            default -> throw new IllegalStateException("applikasjonsnavn er satt til " + applikasjon + " som ikke er en støttet verdi");
        };
    }

    @Produces
    @ApplicationScoped
    public HendelseReader produce() {
        return hendelseReader;
    }
}
