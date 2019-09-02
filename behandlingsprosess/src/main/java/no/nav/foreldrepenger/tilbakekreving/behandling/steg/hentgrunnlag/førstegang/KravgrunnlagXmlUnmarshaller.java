package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.SAXException;

import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlag;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagMelding;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.integrasjon.felles.ws.JaxbHelper;

public class KravgrunnlagXmlUnmarshaller {

    private static final String XSD_PLASSERING = "xsd/no/nav/melding/virksomhet/tilbakekreving/kravgrunnlag_detalj.xsd";

    public static DetaljertKravgrunnlag unmarshall(Long mottattXmlId, String xml) {

        try {
            DetaljertKravgrunnlagMelding melding = JaxbHelper.unmarshalAndValidateXMLWithStAX(DetaljertKravgrunnlagMelding.class, xml, XSD_PLASSERING);
            DetaljertKravgrunnlag kravgrunnlag = melding.getDetaljertKravgrunnlag();
            if (kravgrunnlag != null) {
                return kravgrunnlag;
            }
            throw KravgrunnlagXmlUnmarshallFeil.FACTORY.meldingUtenKravgrunnlag(mottattXmlId).toException();
        } catch (JAXBException e) {
            throw KravgrunnlagXmlUnmarshallFeil.FACTORY.unmarshallingFeilet(mottattXmlId, e).toException();
        } catch (XMLStreamException e) {
            throw KravgrunnlagXmlUnmarshallFeil.FACTORY.unmarshallingFeilet(mottattXmlId, e).toException();
        } catch (SAXException e) {
            throw KravgrunnlagXmlUnmarshallFeil.FACTORY.unmarshallingFeilet(mottattXmlId, e).toException();
        }
    }

    interface KravgrunnlagXmlUnmarshallFeil extends DeklarerteFeil {
        KravgrunnlagXmlUnmarshallFeil FACTORY = FeilFactory.create(KravgrunnlagXmlUnmarshallFeil.class);

        @TekniskFeil(feilkode = "FPT-764415", feilmelding = "Feil ved unmarshalling av kravgrunnlag med id=%s", logLevel = LogLevel.ERROR)
        Feil unmarshallingFeilet(Long kravgrunnlagXmlId, JAXBException cause);

        @TekniskFeil(feilkode = "FPT-508233", feilmelding = "Feil ved unmarshalling av kravgrunnlag med id=%s", logLevel = LogLevel.ERROR)
        Feil unmarshallingFeilet(Long kravgrunnlagXmlId, XMLStreamException cause);

        @TekniskFeil(feilkode = "FPT-992414", feilmelding = "Feil ved unmarshalling av kravgrunnlag med id=%s", logLevel = LogLevel.ERROR)
        Feil unmarshallingFeilet(Long kravgrunnlagXmlId, SAXException cause);

        @TekniskFeil(feilkode = "FPT-624792", feilmelding = "Mottok kravgrunnlag-melding uten kravgrunnlag", logLevel = LogLevel.WARN)
        Feil meldingUtenKravgrunnlag(Long kravgrunnlagXmlIde);
    }
}
