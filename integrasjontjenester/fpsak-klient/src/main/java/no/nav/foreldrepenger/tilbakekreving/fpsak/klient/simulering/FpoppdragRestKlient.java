package no.nav.foreldrepenger.tilbakekreving.fpsak.klient.simulering;

import java.net.URI;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.simulering.FeilutbetaltePerioderDto;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;

@ApplicationScoped
public class FpoppdragRestKlient {

    private static final Environment ENV = Environment.current();

    private OidcRestClient restClient;

    protected FpoppdragRestKlient() {
        //for cdi proxy
    }

    @Inject
    public FpoppdragRestKlient(OidcRestClient restClient) {
        this.restClient = restClient;
    }

    public Optional<FeilutbetaltePerioderDto> hentFeilutbetaltePerioder(long fpsakBehandlingId) {
        var uri = UriBuilder
                .fromUri(baseUri())
                .path("/fpoppdrag/api")
                .path("/simulering/feilutbetalte-perioder")
                .build();
        return restClient.postReturnsOptional(uri, new BehandlingIdDto(fpsakBehandlingId), FeilutbetaltePerioderDto.class);
    }

    private static URI baseUri() {
        return ENV.getProperty("fpoppdrag.base.url", URI.class);
    }
}
