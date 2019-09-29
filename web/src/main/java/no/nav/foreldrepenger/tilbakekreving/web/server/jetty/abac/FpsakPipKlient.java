package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac;

import java.net.URI;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.integrasjon.rest.SystemUserOidcRestClient;
import no.nav.vedtak.konfig.PropertyUtil;

@ApplicationScoped
public class FpsakPipKlient {

    private static final Logger logger = LoggerFactory.getLogger(FpsakPipKlient.class);

    private static final String FPSAK_BASE_URL = "http://fpsak";
    private static final String FPSAK_OVERRIDE_URL = "fpsak.override.url";

    private static final String PIP_BEHANDLING_ENDPOINT = "/fpsak/api/pip/pipdata-for-behandling";

    private SystemUserOidcRestClient restClient;

    private URI endpointFpsakBehandlingPip;

    public FpsakPipKlient() {
        // CDI
    }

    @Inject
    public FpsakPipKlient(SystemUserOidcRestClient restClient) {
        this.restClient = restClient;
        URI baseUri = baseUri();
        this.endpointFpsakBehandlingPip = UriBuilder.fromUri(baseUri).path(PIP_BEHANDLING_ENDPOINT).build();
    }

    public PipDto hentPipdataForFpsakBehandling(UUID behandlingUUid) {
        URI uri = UriBuilder.fromUri(endpointFpsakBehandlingPip)
            .queryParam("behandlingUuid", behandlingUUid)
            .build();
        return restClient.get(uri, PipDto.class);
    }

    private URI baseUri() {
        String override = PropertyUtil.getProperty(FPSAK_OVERRIDE_URL);
        if (override != null && !override.isEmpty()) {
            logger.warn("Overstyrer fpsak base URL med {}", override);
            return URI.create(override);
        }
        return URI.create(FPSAK_BASE_URL);
    }
}
