package no.nav.journalpostapi;


import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import no.nav.journalpostapi.dto.opprett.OpprettJournalpostRequest;
import no.nav.journalpostapi.dto.opprett.OpprettJournalpostResponse;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@ApplicationScoped
@RestClientConfig(tokenConfig = TokenFlow.STS_CC, endpointProperty = "dokarkiv.base.url", endpointDefault = "http://dokarkiv.teamdokumenthandtering")
public class JournalpostApiKlient {

    private RestClient restClient;
    private URI target;

    JournalpostApiKlient() {
        //for CDI proxy
    }

    @Inject
    public JournalpostApiKlient(RestClient restClient) {
        this.restClient = restClient;
        var endpoint = RestConfig.endpointFromAnnotation(JournalpostApiKlient.class);
        this.target = URI.create(endpoint.toString() + "/rest/journalpostapi/v1/journalpost");
    }

    public OpprettJournalpostResponse opprettJournalpost(OpprettJournalpostRequest request, boolean forsøkFerdigstill) {
        var opprett = forsøkFerdigstill ? UriBuilder.fromUri(target).queryParam("forsoekFerdigstill", "true").build() : target;
        var rrequest = RestRequest.newPOSTJson(request, opprett, JournalpostApiKlient.class);
        return restClient.sendExpectConflict(rrequest, OpprettJournalpostResponse.class);
    }
}
