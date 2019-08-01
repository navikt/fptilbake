package no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.task;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import no.nav.tilbakekreving.tilbakekrevingsvedtak.vedtak.v1.TilbakekrevingsvedtakDto;

public class TilbakekrevingsvedtakMarshaller {

    private static volatile JAXBContext context;

    private TilbakekrevingsvedtakMarshaller() {
        //hindrer instansiering
    }

    public static String marshall(long behandlingId, TilbakekrevingsvedtakDto vedtak) {
        //HAXX marshalling løses normalt sett ikke slik som dette. Se JaxbHelper for normaltilfeller.
        //HAXX gjør her marshalling uten kobling til skjema, siden skjema som brukes ikke er egnet for å
        //HAXX konvertere til streng. Skjemaet er bare egnet for å bruke mot WS.

        QName qname = new QName(TilbakekrevingsvedtakDto.class.getSimpleName());
        JAXBElement<TilbakekrevingsvedtakDto> jaxbelement = new JAXBElement<>(qname, TilbakekrevingsvedtakDto.class, vedtak);
        try {
            Marshaller marshaller = getContext().createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
            StringWriter stringWriter = new StringWriter();
            marshaller.marshal(jaxbelement, stringWriter);
            return stringWriter.toString();
        } catch (JAXBException e) {
            throw SendØkonomiTibakekerevingsVedtakTask.SendØkonomiTilbakekrevingVedtakTaskFeil.FACTORY.kunneIkkeMarshalleVedtakXml(behandlingId, e).toException();
        }
    }

    private static JAXBContext getContext() throws JAXBException {
        if (context == null) {
            context = JAXBContext.newInstance(TilbakekrevingsvedtakDto.class);
        }
        return context;
    }
}
