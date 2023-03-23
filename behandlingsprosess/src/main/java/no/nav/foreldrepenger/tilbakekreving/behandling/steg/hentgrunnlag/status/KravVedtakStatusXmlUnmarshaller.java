package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.status;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.SAXException;

import no.nav.foreldrepenger.xmlutils.JaxbHelper;
import no.nav.tilbakekreving.status.v1.EndringKravOgVedtakstatus;
import no.nav.tilbakekreving.status.v1.KravOgVedtakstatus;
import no.nav.vedtak.exception.TekniskException;

public class KravVedtakStatusXmlUnmarshaller {

    private KravVedtakStatusXmlUnmarshaller() {
    }

    private static final String XSD_PLASSERING = "xsd/krav_og_vedtakstatus.xsd";
    private static final String FEIL_VED_UNMARSHALLING_AV_KRAV_OG_VEDTAKSTATUS_XML = "Feil ved unmarshalling av kravOgVedtakstatusXml med id=%s";

    public static KravOgVedtakstatus unmarshall(Long mottattXmlId, String xml) {

        try {
            var melding = JaxbHelper.unmarshalAndValidateXMLWithStAX(EndringKravOgVedtakstatus.class, xml, XSD_PLASSERING);
            var kravOgVedtakstatus = melding.getKravOgVedtakstatus();
            if (kravOgVedtakstatus != null) {
                return kravOgVedtakstatus;
            }
            throw new TekniskException("FPT-624793", String.format("Mottok kravOgVedtakstatus-melding id=%s uten kravOgVedtakstatus", mottattXmlId));
        } catch (JAXBException e) {
            throw new TekniskException("FPT-764416", String.format(FEIL_VED_UNMARSHALLING_AV_KRAV_OG_VEDTAKSTATUS_XML, mottattXmlId), e);
        } catch (XMLStreamException e) {
            throw new TekniskException("FPT-508234", String.format(FEIL_VED_UNMARSHALLING_AV_KRAV_OG_VEDTAKSTATUS_XML, mottattXmlId), e);
        } catch (SAXException e) {
            throw new TekniskException("FPT-992415", String.format(FEIL_VED_UNMARSHALLING_AV_KRAV_OG_VEDTAKSTATUS_XML, mottattXmlId), e);
        }
    }

}
