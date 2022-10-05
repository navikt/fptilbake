package no.nav.journalpostapi;


import javax.enterprise.context.Dependent;
import javax.ws.rs.core.UriBuilder;

import no.nav.journalpostapi.dto.opprett.OpprettJournalpostRequest;
import no.nav.journalpostapi.dto.opprett.OpprettJournalpostResponse;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@Dependent
@RestClientConfig(tokenConfig = TokenFlow.STS_CC, endpointProperty = "dokarkiv.base.url", endpointDefault = "http://dokarkiv.teamdokumenthandtering/rest/journalpostapi/v1/journalpost")
public class JournalpostApiKlient {

    private final RestClient restClient;
    private final RestConfig restConfig;

    public JournalpostApiKlient() {
        this.restClient = RestClient.client();
        this.restConfig = RestConfig.forClient(this.getClass());
    }

    public OpprettJournalpostResponse opprettJournalpost(OpprettJournalpostRequest request, boolean forsøkFerdigstill) {
        var opprett = forsøkFerdigstill ? UriBuilder.fromUri(restConfig.endpoint()).queryParam("forsoekFerdigstill", "true").build() : restConfig.endpoint();
        var rrequest = RestRequest.newPOSTJson(request, opprett, restConfig);
        return restClient.sendExpectConflict(rrequest, OpprettJournalpostResponse.class);
    }
}
