package no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import no.nav.tilbakekreving.tilbakekrevingsvedtak.vedtak.v1.TilbakekrevingsvedtakDto;
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
            throw TilbakekrevingsvedtakMarshallerFeil.FACTORY.kunneIkkeMarshalleVedtakXml(behandlingId, e).toException();
        }
    }

    public static TilbakekrevingsvedtakDto unmarshall(String xml) {
        try {
            Unmarshaller unmarshaller = getContext().createUnmarshaller();
            return (TilbakekrevingsvedtakDto) unmarshaller.unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            throw TilbakekrevingsvedtakMarshallerFeil.FACTORY.kunneIkkeUnmarshalleVedtakXml(e).toException();
        }
    }

    private static JAXBContext getContext() throws JAXBException {
        if (context == null) {
            context = JAXBContext.newInstance(TilbakekrevingsvedtakDto.class);
        }
        return context;
    }

    interface TilbakekrevingsvedtakMarshallerFeil extends DeklarerteFeil {

        TilbakekrevingsvedtakMarshallerFeil FACTORY = FeilFactory.create(TilbakekrevingsvedtakMarshallerFeil.class);

        @TekniskFeil(feilkode = "FPT-113616", feilmelding = "Kunne ikke marshalle vedtak. BehandlingId=%s", logLevel = LogLevel.WARN)
        Feil kunneIkkeMarshalleVedtakXml(Long behandlingId, Exception e);

        @TekniskFeil(feilkode = "FPT-511823", feilmelding = "Kunne ikke unmarshalle vedtak. BehandlingId=%s", logLevel = LogLevel.WARN)
        Feil kunneIkkeUnmarshalleVedtakXml(Exception e);

    }
}
