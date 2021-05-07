package no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import no.nav.okonomi.tilbakekrevingservice.TilbakekrevingsvedtakRequest;
import no.nav.vedtak.exception.TekniskException;

public class TilbakekrevingsvedtakMarshaller {

    private static volatile JAXBContext context;

    private TilbakekrevingsvedtakMarshaller() {
        //hindrer instansiering
    }

    public static String marshall(long behandlingId, TilbakekrevingsvedtakRequest request) {
        //HAXX marshalling løses normalt sett ikke slik som dette. Se JaxbHelper for normaltilfeller.
        //HAXX gjør her marshalling uten kobling til skjema, siden skjema som brukes ikke er egnet for å
        //HAXX konvertere til streng. Skjemaet er bare egnet for å bruke mot WS.

        try {
            Marshaller marshaller = getContext().createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
            StringWriter stringWriter = new StringWriter();
            marshaller.marshal(request, stringWriter);
            return stringWriter.toString();
        } catch (JAXBException e) {
            throw new TekniskException("FPT-113616", String.format("Kunne ikke marshalle vedtak for behandlingId=%s", behandlingId), e);
        }
    }

    public static TilbakekrevingsvedtakRequest unmarshall(String xml, Long xmlId, Long behandlingId) {
        try {
            Unmarshaller unmarshaller = getContext().createUnmarshaller();
            return (TilbakekrevingsvedtakRequest) unmarshaller.unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            throw new TekniskException("FPT-511823", String.format("Kunne ikke unmarshalle vedtak for behandlingId=%s xmlId=%s", xmlId, behandlingId), e);
        }
    }

    private static JAXBContext getContext() throws JAXBException {
        if (context == null) {
            context = JAXBContext.newInstance(TilbakekrevingsvedtakRequest.class);
        }
        return context;
    }


}
