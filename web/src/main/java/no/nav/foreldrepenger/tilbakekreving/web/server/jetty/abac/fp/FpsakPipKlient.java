package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.fp;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.enterprise.context.Dependent;
import javax.ws.rs.core.UriBuilder;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.vedtak.felles.integrasjon.rest.FpApplication;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;
import no.nav.vedtak.sikkerhet.abac.pipdata.AbacPipDto;

@Dependent
@RestClientConfig(tokenConfig = TokenFlow.STS_CC, application = FpApplication.FPSAK)
public class FpsakPipKlient {

    private static final String PIP_PATH = "/api/pip";

    private final RestClient restClient;
    private final RestConfig restConfig;

    public FpsakPipKlient() {
        this.restClient = RestClient.client();
        this.restConfig = RestConfig.forClient(FpsakPipKlient.class);
    }

    public AbacPipDto hentPipdataForFpsakBehandling(UUID behandlingUUid) {
        var uri = UriBuilder.fromUri(restConfig.fpContextPath())
            .path(PIP_PATH)
            .path("/pipdata-for-behandling-appintern")
            .queryParam("behandlingUuid", behandlingUUid.toString())
            .build();
        return restClient.send(RestRequest.newGET(uri, restConfig), AbacPipDto.class);
    }

    public Set<String> hentAktørIderSomString(Saksnummer saksnummer) {
        var uri = UriBuilder.fromUri(restConfig.fpContextPath())
                .path(PIP_PATH)
                .path("/aktoer-for-sak")
                .queryParam("saksnummer", saksnummer.getVerdi())
                .build();
        return restClient.send(RestRequest.newGET(uri, restConfig), HashSet.class);
    }
}
