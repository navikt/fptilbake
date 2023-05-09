package no.nav.foreldrepenger.tilbakekreving.domene.person.pdl;

import no.nav.vedtak.felles.integrasjon.person.AbstractPersonKlient;
import no.nav.vedtak.felles.integrasjon.person.Persondata;
import no.nav.vedtak.felles.integrasjon.person.Tema;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

// Ikke bean - trenger sette tema ved bruk
@RestClientConfig(tokenConfig = TokenFlow.ADAPTIVE_ADD_CONSUMER, endpointProperty = "pdl.base.url", endpointDefault = "http://pdl-api.pdl/graphql",
    scopesProperty = "pdl.scopes", scopesDefault = "api://prod-fss.pdl.pdl-api/.default")
public class PdlKlient extends AbstractPersonKlient {

    public PdlKlient(Tema tema, Persondata.Ytelse ytelse) {
        super(RestClient.client(), tema, ytelse);
    }
}
