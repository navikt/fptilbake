package no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import no.nav.okonomi.tilbakekrevingservice.TilbakekrevingsvedtakRequest;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

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
            throw TilbakekrevingsvedtakMarshallerFeil.FACTORY.kunneIkkeMarshalleVedtakXml(behandlingId, e).toException();
        }
    }

    public static TilbakekrevingsvedtakRequest unmarshall(String xml, Long xmlId, Long behandlingId) {
        try {
            Unmarshaller unmarshaller = getContext().createUnmarshaller();
            return (TilbakekrevingsvedtakRequest) unmarshaller.unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            throw TilbakekrevingsvedtakMarshallerFeil.FACTORY.kunneIkkeUnmarshalleVedtakXml(xmlId, behandlingId, e).toException();
        }
    }

    private static JAXBContext getContext() throws JAXBException {
        if (context == null) {
            context = JAXBContext.newInstance(TilbakekrevingsvedtakRequest.class);
        }
        return context;
    }

    interface TilbakekrevingsvedtakMarshallerFeil extends DeklarerteFeil {

        TilbakekrevingsvedtakMarshallerFeil FACTORY = FeilFactory.create(TilbakekrevingsvedtakMarshallerFeil.class);

        @TekniskFeil(feilkode = "FPT-113616", feilmelding = "Kunne ikke marshalle vedtak for behandlingId=%s", logLevel = LogLevel.WARN)
        Feil kunneIkkeMarshalleVedtakXml(Long behandlingId, JAXBException e);

        @TekniskFeil(feilkode = "FPT-511823", feilmelding = "Kunne ikke unmarshalle vedtak for behandlingId=%s xmlId=%s", logLevel = LogLevel.WARN)
        Feil kunneIkkeUnmarshalleVedtakXml(Long behandlingId, Long xmlId, JAXBException e);

    }
}
