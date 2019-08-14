package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.status;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.SAXException;

import no.nav.tilbakekreving.status.v1.EndringKravOgVedtakstatus;
import no.nav.tilbakekreving.status.v1.KravOgVedtakstatus;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.integrasjon.felles.ws.JaxbHelper;

class KravVedtakStatusXmlUnmarshaller {

    private static final String XSD_PLASSERING = "xsd/no/nav/melding/virksomhet/tilbakekreving/krav_og_vedtakstatus.xsd";

    public static KravOgVedtakstatus unmarshall(Long mottattXmlId, String xml) {

        try {
            EndringKravOgVedtakstatus melding = JaxbHelper.unmarshalAndValidateXMLWithStAX(EndringKravOgVedtakstatus.class, xml, XSD_PLASSERING);
            KravOgVedtakstatus kravOgVedtakstatus = melding.getKravOgVedtakstatus();
            if (kravOgVedtakstatus != null) {
                return kravOgVedtakstatus;
            }
            throw KravVedtakStatusXmlUnmarshallFeil.FACTORY.meldingUtenKravVedtakStatus(mottattXmlId).toException();
        } catch (JAXBException e) {
            throw KravVedtakStatusXmlUnmarshallFeil.FACTORY.unmarshallingFeilet(mottattXmlId, e).toException();
        } catch (XMLStreamException e) {
            throw KravVedtakStatusXmlUnmarshallFeil.FACTORY.unmarshallingFeilet(mottattXmlId, e).toException();
        } catch (SAXException e) {
            throw KravVedtakStatusXmlUnmarshallFeil.FACTORY.unmarshallingFeilet(mottattXmlId, e).toException();
        }
    }

    interface KravVedtakStatusXmlUnmarshallFeil extends DeklarerteFeil {
        KravVedtakStatusXmlUnmarshallFeil FACTORY = FeilFactory.create(KravVedtakStatusXmlUnmarshallFeil.class);

        @TekniskFeil(feilkode = "FPT-764416", feilmelding = "Feil ved unmarshalling av kravOgVedtakstatusXml med id=%s", logLevel = LogLevel.ERROR)
        Feil unmarshallingFeilet(Long kravgrunnlagXmlId, JAXBException cause);

        @TekniskFeil(feilkode = "FPT-508234", feilmelding = "Feil ved unmarshalling av kravOgVedtakstatusXml med id=%s", logLevel = LogLevel.ERROR)
        Feil unmarshallingFeilet(Long kravgrunnlagXmlId, XMLStreamException cause);

        @TekniskFeil(feilkode = "FPT-992415", feilmelding = "Feil ved unmarshalling av kravOgVedtakstatusXml med id=%s", logLevel = LogLevel.ERROR)
        Feil unmarshallingFeilet(Long kravgrunnlagXmlId, SAXException cause);

        @TekniskFeil(feilkode = "FPT-624793", feilmelding = "Mottok kravOgVedtakstatus-melding uten kravOgVedtakstatus", logLevel = LogLevel.WARN)
        Feil meldingUtenKravVedtakStatus(Long kravgrunnlagXmlIde);
    }
}
