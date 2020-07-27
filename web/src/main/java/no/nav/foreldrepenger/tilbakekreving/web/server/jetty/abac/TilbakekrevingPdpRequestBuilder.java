package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac;

import no.nav.vedtak.sikkerhet.abac.AbacAttributtSamling;
import no.nav.vedtak.sikkerhet.abac.PdpRequest;

public interface TilbakekrevingPdpRequestBuilder {
    PdpRequest lagPdpRequest(AbacAttributtSamling attributter);
}
