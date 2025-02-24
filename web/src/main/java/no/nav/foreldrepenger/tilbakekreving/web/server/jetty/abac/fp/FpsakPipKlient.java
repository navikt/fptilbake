package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.fp;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.enterprise.context.Dependent;
import jakarta.ws.rs.core.UriBuilder;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.vedtak.felles.integrasjon.rest.FpApplication;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipAktørId;

@Dependent
@RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC, application = FpApplication.FPSAK)
public class FpsakPipKlient {

    private static final String PIP_PATH = "/api/pip";
    private static final String AKTOER_FOR_SAK = "/aktoer-for-sak";
    private static final String AKTOER_FOR_BEHANDLING = "/aktoer-for-behandling";

    private final RestClient restClient;
    private final RestConfig restConfig;

    public FpsakPipKlient() {
        this.restClient = RestClient.client();
        this.restConfig = RestConfig.forClient(FpsakPipKlient.class);
    }

    public Set<PipAktørId> hentAktørIdForBehandling(UUID behandlingUUid) {
        var uri = UriBuilder.fromUri(restConfig.fpContextPath())
            .path(PIP_PATH)
            .path(AKTOER_FOR_BEHANDLING)
            .queryParam("behandlingUuid", behandlingUUid.toString())
            .build();
        var respons = restClient.sendReturnList(RestRequest.newGET(uri, restConfig), PipAktørId.class);
        return new HashSet<>(respons);
    }

    public Set<String> hentAktørIderSomString(Saksnummer saksnummer) {
        var uri = UriBuilder.fromUri(restConfig.fpContextPath())
                .path(PIP_PATH)
                .path(AKTOER_FOR_SAK)
                .queryParam("saksnummer", saksnummer.getVerdi())
                .build();
        var aktører = restClient.sendReturnList(RestRequest.newGET(uri, restConfig), String.class);
        return new HashSet<>(aktører);
    }
}
