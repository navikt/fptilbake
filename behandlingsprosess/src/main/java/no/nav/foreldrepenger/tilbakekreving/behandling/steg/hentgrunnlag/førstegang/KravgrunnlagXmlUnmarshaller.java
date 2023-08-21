package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang;

import jakarta.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.SAXException;

import no.nav.foreldrepenger.xmlutils.JaxbHelper;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlag;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagMelding;
import no.nav.vedtak.exception.TekniskException;

public class KravgrunnlagXmlUnmarshaller {

    private static final String XSD_PLASSERING = "xsd/kravgrunnlag_detalj.xsd";

    public static DetaljertKravgrunnlag unmarshall(Long mottattXmlId, String xml) {

        try {
            DetaljertKravgrunnlagMelding melding = JaxbHelper.unmarshalAndValidateXMLWithStAX(DetaljertKravgrunnlagMelding.class, xml, XSD_PLASSERING);
            DetaljertKravgrunnlag kravgrunnlag = melding.getDetaljertKravgrunnlag();
            if (kravgrunnlag != null) {
                return kravgrunnlag;
            }
            throw new TekniskException("FPT-624792", String.format("Mottok kravgrunnlag-melding id=%s uten kravgrunnlag", mottattXmlId));
        } catch (JAXBException e) {
            throw new TekniskException("FPT-764415", String.format("Feil ved unmarshalling av kravgrunnlag med id=%s", mottattXmlId), e);
        } catch (XMLStreamException e) {
            throw new TekniskException("FPT-508233", String.format("Feil ved unmarshalling av kravgrunnlag med id=%s", mottattXmlId), e);
        } catch (SAXException e) {
            throw new TekniskException("FPT-992414", String.format("Feil ved unmarshalling av kravgrunnlag med id=%s", mottattXmlId), e);
        }
    }

}
