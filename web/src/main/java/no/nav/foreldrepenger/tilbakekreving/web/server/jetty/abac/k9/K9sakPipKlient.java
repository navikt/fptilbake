package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.vedtak.felles.integrasjon.rest.SystemUserOidcRestClient;
import no.nav.vedtak.konfig.PropertyUtil;

@ApplicationScoped
public class K9sakPipKlient {

    private static final Logger logger = LoggerFactory.getLogger(K9sakPipKlient.class);

    private static final String FPSAK_BASE_URL = "http://k9sak";
    private static final String FPSAK_OVERRIDE_URL = "k9sak.override.url";

    private static final String PIP_BEHANDLING_ENDPOINT = "/k9/sak/api/pip/pipdata-for-behandling";
    private static final String PIP_SAK_ENDPOINT = "/k9/sak/api/pip/aktoer-for-sak";

    private SystemUserOidcRestClient restClient;

    private URI endpointFpsakBehandlingPip;
    private URI endpointFpsakSakPip;

    public K9sakPipKlient() {
        // CDI
    }

    @Inject
    public K9sakPipKlient(SystemUserOidcRestClient restClient) {
        this.restClient = restClient;
        URI baseUri = baseUri();
        this.endpointFpsakBehandlingPip = UriBuilder.fromUri(baseUri).path(PIP_BEHANDLING_ENDPOINT).build();
        this.endpointFpsakSakPip = UriBuilder.fromUri(baseUri).path(PIP_SAK_ENDPOINT).build();
    }

    public K9PipDto hentPipdataForK9sakBehandling(UUID behandlingUUid) {
        URI uri = UriBuilder.fromUri(endpointFpsakBehandlingPip)
            .queryParam("behandlingUuid", behandlingUUid.toString())
            .build();
        return restClient.get(uri, K9PipDto.class);
    }

    public Set<String> hentAktørIderSomString(Saksnummer saksnummer) {
        URI uri = UriBuilder.fromUri(endpointFpsakSakPip)
            .queryParam("saksnummer", saksnummer.getVerdi())
            .build();

        return restClient.get(uri, HashSet.class);
    }

    public Set<AktørId> hentAktørIder(Saksnummer saksnummer) {
        Set<String> aktørIder = hentAktørIderSomString(saksnummer);
        return aktørIder.stream()
            .map(AktørId::new)
            .collect(Collectors.toSet());
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
