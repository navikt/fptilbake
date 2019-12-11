package no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import no.nav.tilbakekreving.typer.v1.MmelDto;

public class ØkonomiResponsMarshaller {

    private static volatile JAXBContext context;

    private ØkonomiResponsMarshaller() {
        //hindrer instansiering
    }

    public static String marshall(long behandlingId, MmelDto respons) {
        //HAXX marshalling løses normalt sett ikke slik som dette. Se JaxbHelper for normaltilfeller.
        //HAXX gjør her marshalling uten kobling til skjema, siden skjema som brukes ikke er egnet for å
        //HAXX konvertere til streng. Skjemaet er bare egnet for å bruke mot WS.

        QName qname = new QName(MmelDto.class.getSimpleName());
        JAXBElement<MmelDto> jaxbelement = new JAXBElement<>(qname, MmelDto.class, respons);
        try {
            Marshaller marshaller = getContext().createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
            StringWriter stringWriter = new StringWriter();
            marshaller.marshal(jaxbelement, stringWriter);
            return stringWriter.toString();
        } catch (JAXBException e) {
            throw ØkonomiResponsFeil.FACTORY.kunneIkkeMarshalleØkonomiResponsXml(behandlingId, e).toException();
        }
    }

    public static MmelDto unmarshall(String xml) {
        try {
            Unmarshaller unmarshaller = getContext().createUnmarshaller();
            return (MmelDto) unmarshaller.unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            throw ØkonomiResponsFeil.FACTORY.kunneIkkeUnmarshalleØkonomiResponsXml(e).toException();
        }
    }

    private static JAXBContext getContext() throws JAXBException {
        if (context == null) {
            context = JAXBContext.newInstance(MmelDto.class);
        }
        return context;
    }
}
