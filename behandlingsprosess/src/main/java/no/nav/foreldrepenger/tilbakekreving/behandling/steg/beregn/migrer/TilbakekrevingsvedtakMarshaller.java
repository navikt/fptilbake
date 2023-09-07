package no.nav.foreldrepenger.tilbakekreving.behandling.steg.beregn.migrer;

import java.io.StringReader;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import no.nav.tilbakekreving.tilbakekrevingsvedtak.vedtak.v1.TilbakekrevingsvedtakRequest;
import no.nav.vedtak.exception.TekniskException;

public class TilbakekrevingsvedtakMarshaller {

    private static volatile JAXBContext context;

    private TilbakekrevingsvedtakMarshaller() {
        //hindrer instansiering
    }

    public static TilbakekrevingsvedtakRequest unmarshall(String xml, Long xmlId, Long behandlingId) {
        try {
            Unmarshaller unmarshaller = getContext().createUnmarshaller();
            return (TilbakekrevingsvedtakRequest) unmarshaller.unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            throw new TekniskException("FPT-511823", String.format("Kunne ikke unmarshalle vedtak for behandlingId=%s xmlId=%s", xmlId, behandlingId),
                e);
        }
    }

    private static JAXBContext getContext() throws JAXBException {
        if (context == null) {
            context = JAXBContext.newInstance(TilbakekrevingsvedtakRequest.class);
        }
        return context;
    }


}
