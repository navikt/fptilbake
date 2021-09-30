package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9;

import java.net.URI;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.felles.integrasjon.rest.SystemUserOidcRestClient;

@ApplicationScoped
public class K9sakPipKlient {

    private static final Logger logger = LoggerFactory.getLogger(K9sakPipKlient.class);

    private static final String K9SAK_BASE_URL = "http://k9-sak";
    private static final String K9SAK_OVERRIDE_URL = "k9sak.override.url";

    private static final String PIP_BEHANDLING_ENDPOINT = "/k9/sak/api/pip/pipdata-for-behandling";

    private SystemUserOidcRestClient restClient;

    private URI endpointK9sakBehandlingPip;

    public K9sakPipKlient() {
        // CDI
    }

    @Inject
    public K9sakPipKlient(SystemUserOidcRestClient restClient) {
        this.restClient = restClient;
        URI baseUri = baseUri();
        this.endpointK9sakBehandlingPip = UriBuilder.fromUri(baseUri).path(PIP_BEHANDLING_ENDPOINT).build();
    }

    public K9PipDto hentPipdataForK9sakBehandling(UUID behandlingUUid) {
        URI uri = UriBuilder.fromUri(endpointK9sakBehandlingPip)
            .queryParam("behandlingUuid", behandlingUUid.toString())
            .build();
        return restClient.get(uri, K9PipDto.class);
    }

    private URI baseUri() {
        String override = Environment.current().getProperty(K9SAK_OVERRIDE_URL);
        if (override != null && !override.isEmpty()) {
            logger.warn("Overstyrer k9sak base URL med {}", override);
            return URI.create(override);
        }
        return URI.create(K9SAK_BASE_URL);
    }
}
