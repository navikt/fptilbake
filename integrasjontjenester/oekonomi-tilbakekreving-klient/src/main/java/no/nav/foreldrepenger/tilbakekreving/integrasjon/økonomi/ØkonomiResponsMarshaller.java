package no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import no.nav.okonomi.tilbakekrevingservice.TilbakekrevingsvedtakResponse;

public class ØkonomiResponsMarshaller {

    private static volatile JAXBContext context;

    private ØkonomiResponsMarshaller() {
        //hindrer instansiering
    }

    public static String marshall(TilbakekrevingsvedtakResponse respons, Long behandlingId) {
        //HAXX marshalling løses normalt sett ikke slik som dette. Se JaxbHelper for normaltilfeller.
        //HAXX gjør her marshalling uten kobling til skjema, siden skjema som brukes ikke er egnet for å
        //HAXX konvertere til streng. Skjemaet er bare egnet for å bruke mot WS.

        try {
            Marshaller marshaller = getContext().createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
            StringWriter stringWriter = new StringWriter();
            marshaller.marshal(respons, stringWriter);
            return stringWriter.toString();
        } catch (JAXBException e) {
            throw ØkonomiResponsFeil.FACTORY.kunneIkkeMarshalleØkonomiResponsXml(behandlingId, e).toException();
        }
    }

    public static TilbakekrevingsvedtakResponse unmarshall(String xml, Long behandlingId, Long xmlId) {
        try {
            Unmarshaller unmarshaller = getContext().createUnmarshaller();
            return (TilbakekrevingsvedtakResponse) unmarshaller.unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            throw ØkonomiResponsFeil.FACTORY.kunneIkkeUnmarshalleØkonomiResponsXml(behandlingId, xmlId, e).toException();
        }
    }

    private static JAXBContext getContext() throws JAXBException {
        if (context == null) {
            context = JAXBContext.newInstance(TilbakekrevingsvedtakResponse.class);
        }
        return context;
    }
}
