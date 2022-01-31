package no.nav.journalpostapi;


import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.journalpostapi.dto.opprett.OpprettJournalpostRequest;
import no.nav.journalpostapi.dto.opprett.OpprettJournalpostResponse;
import no.nav.vedtak.felles.integrasjon.rest.SystemUserOidcRestClient;

@ApplicationScoped
public class JournalpostApiKlient {

    private static final Environment ENV = Environment.current();

    private SystemUserOidcRestClient oidcRestClient; //FIXME denne skal settes til OidcRestClient når journalføring kun utføres i prosesstask

    JournalpostApiKlient() {
        //for CDI proxy
    }

    @Inject
    public JournalpostApiKlient(SystemUserOidcRestClient oidcRestClient) {
        this.oidcRestClient = oidcRestClient;
    }

    public OpprettJournalpostResponse opprettJournalpost(OpprettJournalpostRequest request, boolean forsøkFerdigstill) {
        // FIXME : Rydd opp journalpostapi.override.url etter alle har tatt i bruk dokarkiv.base.url
        URI uri = UriBuilder.fromUri(ENV.getProperty("dokarkiv.base.url", ENV.getProperty("journalpostapi.override.url")))
                .path("/rest/journalpostapi/v1/journalpost")
                .queryParam("forsoekFerdigstill", forsøkFerdigstill)
                .build();
        return oidcRestClient.post(uri, request, OpprettJournalpostResponse.class);
    }
}
