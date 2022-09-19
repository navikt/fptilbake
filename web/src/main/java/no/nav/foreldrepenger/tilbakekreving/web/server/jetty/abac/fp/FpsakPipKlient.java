package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.fp;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.vedtak.felles.integrasjon.rest.FpApplication;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;
import no.nav.vedtak.sikkerhet.abac.pipdata.AbacPipDto;

@ApplicationScoped
@RestClientConfig(tokenConfig = TokenFlow.STS_CC, application = FpApplication.FPSAK)
public class FpsakPipKlient {

    private static final String PIP_PATH = "/api/pip";

    private RestClient restClient;
    private URI contextPath;

    protected FpsakPipKlient() {
        // CDI
    }

    @Inject
    public FpsakPipKlient(RestClient restClient) {
        this.restClient = restClient;
        this.contextPath = RestConfig.endpointFromAnnotation(FpsakPipKlient.class);
    }

    public AbacPipDto hentPipdataForFpsakBehandling(UUID behandlingUUid) {
        var uri = UriBuilder.fromUri(contextPath)
            .path(PIP_PATH)
            .path("/pipdata-for-behandling-appintern")
            .queryParam("behandlingUuid", behandlingUUid.toString())
            .build();
        return restClient.send(RestRequest.newGET(uri, TokenFlow.STS_CC, null), AbacPipDto.class);
    }

    public Set<String> hentAktørIderSomString(Saksnummer saksnummer) {
        var uri = UriBuilder.fromUri(contextPath)
                .path(PIP_PATH)
                .path("/aktoer-for-sak")
                .queryParam("saksnummer", saksnummer.getVerdi())
                .build();
        return restClient.send(RestRequest.newGET(uri, TokenFlow.STS_CC, null), HashSet.class);
    }
}
