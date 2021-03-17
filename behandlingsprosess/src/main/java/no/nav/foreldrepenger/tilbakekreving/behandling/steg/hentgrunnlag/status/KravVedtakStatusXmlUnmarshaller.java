package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.status;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.SAXException;

import no.nav.tilbakekreving.status.v1.EndringKravOgVedtakstatus;
import no.nav.tilbakekreving.status.v1.KravOgVedtakstatus;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.xmlutils.JaxbHelper;

public class KravVedtakStatusXmlUnmarshaller {

    private static final String XSD_PLASSERING = "xsd/krav_og_vedtakstatus.xsd";

    public static KravOgVedtakstatus unmarshall(Long mottattXmlId, String xml) {

        try {
            EndringKravOgVedtakstatus melding = JaxbHelper.unmarshalAndValidateXMLWithStAX(EndringKravOgVedtakstatus.class, xml, XSD_PLASSERING);
            KravOgVedtakstatus kravOgVedtakstatus = melding.getKravOgVedtakstatus();
            if (kravOgVedtakstatus != null) {
                return kravOgVedtakstatus;
            }
            throw new TekniskException("FPT-624793", String.format("Mottok kravOgVedtakstatus-melding id=%s uten kravOgVedtakstatus", mottattXmlId));
        } catch (JAXBException e) {
            throw new TekniskException("FPT-764416", String.format("Feil ved unmarshalling av kravOgVedtakstatusXml med id=%s", mottattXmlId), e);
        } catch (XMLStreamException e) {
            throw new TekniskException("FPT-508234", String.format("Feil ved unmarshalling av kravOgVedtakstatusXml med id=%s", mottattXmlId), e);
        } catch (SAXException e) {
            throw new TekniskException("FPT-992415", String.format("Feil ved unmarshalling av kravOgVedtakstatusXml med id=%s", mottattXmlId), e);
        }
    }

}
