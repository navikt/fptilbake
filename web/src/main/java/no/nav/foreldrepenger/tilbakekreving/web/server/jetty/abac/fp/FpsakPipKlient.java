package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.fp;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.vedtak.felles.integrasjon.rest.SystemUserOidcRestClient;
import no.nav.vedtak.sikkerhet.abac.pipdata.AbacPipDto;

@ApplicationScoped
public class FpsakPipKlient {

    private static final Environment ENV = Environment.current();
    private static final String FPSAK_API_PATH = "/fpsak/api";

    private SystemUserOidcRestClient restClient;

    protected FpsakPipKlient() {
        // CDI
    }

    @Inject
    public FpsakPipKlient(SystemUserOidcRestClient restClient) {
        this.restClient = restClient;
    }

    public AbacPipDto hentPipdataForFpsakBehandling(UUID behandlingUUid) {
        var uri = UriBuilder
                .fromUri(baseUri())
                .path(FPSAK_API_PATH)
                .path("/pip/pipdata-for-behandling-appintern")
                .queryParam("behandlingUuid", behandlingUUid.toString())
                .build();
        return restClient.get(uri, AbacPipDto.class);
    }

    public Set<String> hentAktørIderSomString(Saksnummer saksnummer) {
        var uri = UriBuilder
                .fromUri(baseUri())
                .path(FPSAK_API_PATH)
                .path("/pip/aktoer-for-sak")
                .queryParam("saksnummer", saksnummer.getVerdi())
                .build();
        return restClient.get(uri, HashSet.class);
    }

    private URI baseUri() {
        return ENV.getProperty("fpsak.base.url", URI.class);
    }
}
