package no.nav.foreldrepenger.tilbakekreving.behandling.steg.henlegg;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import no.nav.tilbakekreving.kravgrunnlag.annuller.v1.AnnullerKravgrunnlagDto;

public class TilbakekrevingsAnnuleregrunnlagMarshaller {

    private static volatile JAXBContext context;

    private TilbakekrevingsAnnuleregrunnlagMarshaller() {
        //hindrer instansiering
    }

    public static String marshall(long behandlingId, AnnullerKravgrunnlagDto annulereKravgrunnlag) {
        //HAXX marshalling løses normalt sett ikke slik som dette. Se JaxbHelper for normaltilfeller.
        //HAXX gjør her marshalling uten kobling til skjema, siden skjema som brukes ikke er egnet for å
        //HAXX konvertere til streng. Skjemaet er bare egnet for å bruke mot WS.

        QName qname = new QName(AnnullerKravgrunnlagDto.class.getSimpleName());
        JAXBElement<AnnullerKravgrunnlagDto> jaxbelement = new JAXBElement<>(qname, AnnullerKravgrunnlagDto.class, annulereKravgrunnlag);
        try {
            Marshaller marshaller = getContext().createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
            StringWriter stringWriter = new StringWriter();
            marshaller.marshal(jaxbelement, stringWriter);
            return stringWriter.toString();
        } catch (JAXBException e) {
            throw AnnullereKravgrunnlagTask.AnnulereKravgrunnlagTaskFeil.FACTORY.kunneIkkeMarshalleAnnulereGrunnlagXml(behandlingId, e).toException();
        }
    }

    private static JAXBContext getContext() throws JAXBException {
        if (context == null) {
            context = JAXBContext.newInstance(AnnullerKravgrunnlagDto.class);
        }
        return context;
    }
}
