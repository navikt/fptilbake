package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.FellesTestOppsett;

class KravgrunnlagXmlUnmarshallerTest extends FellesTestOppsett {

    @Test
    void skal_unmarshalle() {
        String xml = getInputXML("xml/kravgrunnlag_detaljert.xml");
        KravgrunnlagXmlUnmarshaller.unmarshall(0L, xml, true);
    }

}
