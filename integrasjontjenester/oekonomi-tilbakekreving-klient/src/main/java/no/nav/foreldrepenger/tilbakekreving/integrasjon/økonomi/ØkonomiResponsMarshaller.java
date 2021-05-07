package no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import no.nav.okonomi.tilbakekrevingservice.TilbakekrevingsvedtakResponse;
import no.nav.vedtak.exception.TekniskException;

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
            throw new TekniskException("FPT-113618", String.format("Kunne ikke marshalle respons fra økonomi for behandlingId=%s", behandlingId), e);
        }
    }

    public static TilbakekrevingsvedtakResponse unmarshall(String xml, Long behandlingId, Long xmlId) {
        try {
            Unmarshaller unmarshaller = getContext().createUnmarshaller();
            return (TilbakekrevingsvedtakResponse) unmarshaller.unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            throw new TekniskException("FPT-176103", String.format("Kunne ikke unmarshalle respons fra økonomi for behandlingId=%s xmlId=%s", behandlingId, xmlId), e);
        }
    }

    private static JAXBContext getContext() throws JAXBException {
        if (context == null) {
            context = JAXBContext.newInstance(TilbakekrevingsvedtakResponse.class);
        }
        return context;
    }
}
