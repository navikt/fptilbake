package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.fp;

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

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.vedtak.felles.integrasjon.rest.SystemUserOidcRestClient;

@ApplicationScoped
public class FpsakPipKlient {

    private static final Logger logger = LoggerFactory.getLogger(FpsakPipKlient.class);

    private static final String FPSAK_BASE_URL = "http://fpsak";
    private static final String FPSAK_OVERRIDE_URL = "fpsak.override.url";

    private static final String PIP_BEHANDLING_ENDPOINT = "/fpsak/api/pip/pipdata-for-behandling";
    private static final String PIP_SAK_ENDPOINT = "/fpsak/api/pip/aktoer-for-sak";

    private SystemUserOidcRestClient restClient;

    private URI endpointFpsakBehandlingPip;
    private URI endpointFpsakSakPip;

    public FpsakPipKlient() {
        // CDI
    }

    @Inject
    public FpsakPipKlient(SystemUserOidcRestClient restClient) {
        this.restClient = restClient;
        URI baseUri = baseUri();
        this.endpointFpsakBehandlingPip = UriBuilder.fromUri(baseUri).path(PIP_BEHANDLING_ENDPOINT).build();
        this.endpointFpsakSakPip = UriBuilder.fromUri(baseUri).path(PIP_SAK_ENDPOINT).build();
    }

    public PipDto hentPipdataForFpsakBehandling(UUID behandlingUUid) {
        URI uri = UriBuilder.fromUri(endpointFpsakBehandlingPip)
                .queryParam("behandlingUuid", behandlingUUid.toString())
                .build();
        return restClient.get(uri, PipDto.class);
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
        String override = Environment.current().getProperty(FPSAK_OVERRIDE_URL);
        if (override != null && !override.isEmpty()) {
            logger.warn("Overstyrer fpsak base URL med {}", override);
            return URI.create(override);
        }
        return URI.create(FPSAK_BASE_URL);
    }
}
