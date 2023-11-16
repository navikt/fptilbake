package no.nav.foreldrepenger.tilbakekreving.behandling.steg.beregn.migrer;

import java.io.StringReader;

import javax.xml.transform.stream.StreamSource;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import no.nav.tilbakekreving.tilbakekrevingsvedtak.vedtak.v1.Tilbakekrevingsvedtak;
import no.nav.tilbakekreving.tilbakekrevingsvedtak.vedtak.v1.TilbakekrevingsvedtakRequest;
import no.nav.vedtak.exception.TekniskException;

public class TilbakekrevingsvedtakMarshaller {

    private static volatile JAXBContext context;

    private TilbakekrevingsvedtakMarshaller() {
        //hindrer instansiering
    }

    public static Tilbakekrevingsvedtak unmarshall(String xml, Long behandlingId) {
        try {
            var unmarshaller = getContext().createUnmarshaller();
            var rewrittenXml = xml.replace("ns4:tilbakekrevingsvedtakRequest", "ns3:tilbakekrevingsvedtakRequest")
                .replace("tilbakekrevingsvedtak>", "ns3:tilbakekrevingsvedtak>");
            var element = unmarshaller.unmarshal(new StreamSource(new StringReader(rewrittenXml)), TilbakekrevingsvedtakRequest.class);
            return element.getValue().getTilbakekrevingsvedtak();
        } catch (JAXBException e) {
            throw new TekniskException("FPT-511823", String.format("Kunne ikke unmarshalle vedtak for behandlingId=%s", behandlingId), e);
        }
    }

    private static JAXBContext getContext() throws JAXBException {
        if (context == null) {
            context = JAXBContext.newInstance(TilbakekrevingsvedtakRequest.class);
        }
        return context;
    }


}
