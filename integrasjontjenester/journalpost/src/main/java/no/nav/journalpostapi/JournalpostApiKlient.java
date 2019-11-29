package no.nav.journalpostapi;


import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import no.nav.journalpostapi.dto.opprett.OpprettJournalpostRequest;
import no.nav.journalpostapi.dto.opprett.OpprettJournalpostResponse;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.felles.integrasjon.rest.SystemUserOidcRestClient;

@ApplicationScoped
public class JournalpostApiKlient {

    private SystemUserOidcRestClient oidcRestClient; //FIXME denne skal settes til OidcRestClient når journalføring kun utføres i prosesstask

    JournalpostApiKlient() {
        //for CDI proxy
    }

    @Inject
    public JournalpostApiKlient(SystemUserOidcRestClient oidcRestClient) {
        this.oidcRestClient = oidcRestClient;
    }

    public OpprettJournalpostResponse opprettJournalpost(OpprettJournalpostRequest request) {
        return opprettJournalpost(request, false);
    }

    public OpprettJournalpostResponse opprettJournalpost(OpprettJournalpostRequest request, boolean forsøkFerdigstill) {
        URI uri = UriBuilder.fromUri(JournalpostApiPlassering.getBaseUrl())
            .path("/rest/journalpostapi/v1/journalpost")
            .queryParam("forsoekFerdigstill", forsøkFerdigstill)
            .build();
        return oidcRestClient.post(uri, request, OpprettJournalpostResponse.class);
    }
}
