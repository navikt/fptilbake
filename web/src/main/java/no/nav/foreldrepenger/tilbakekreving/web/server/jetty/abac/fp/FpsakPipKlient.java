package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.fp;

import jakarta.enterprise.context.Dependent;

import no.nav.vedtak.felles.integrasjon.fpsakpip.AbstractForeldrepengerPipKlient;
import no.nav.vedtak.felles.integrasjon.rest.FpApplication;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@Dependent
@RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC, application = FpApplication.FPSAK)
public class FpsakPipKlient extends AbstractForeldrepengerPipKlient {

}
