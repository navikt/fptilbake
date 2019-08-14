package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang;

import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.FellesTestOppsett;

public class KravgrunnlagXmlUnmarshallerTest extends FellesTestOppsett {

    @Test
    public void skal_unmarshalle() {
        String xml = getInputXML("xml/kravgrunnlag_detaljert.xml");
        KravgrunnlagXmlUnmarshaller.unmarshall(0L, xml);
    }

}
