package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dokdist;


import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@ApplicationScoped
@RestClientConfig(tokenConfig = TokenFlow.STS_CC, endpointProperty = "dokdist.rest.distribuer.journalpost",
    endpointDefault = "http://dokdistfordeling.teamdokumenthandtering/rest/v1/distribuerjournalpost")
public class DokdistKlient {

    private static final Logger logger = LoggerFactory.getLogger(DokdistKlient.class);

    private final RestClient restClient;
    private final RestConfig restConfig;
    private final Fagsystem application;

    public DokdistKlient() {
        this.restClient = RestClient.client();
        this.restConfig = RestConfig.forClient(DokdistKlient.class);
        this.application = ApplicationName.hvilkenTilbake();
    }

    /**
     * Dokumentasjon: https://confluence.adeo.no/pages/viewpage.action?pageId=320039012
     */
    public DistribuerJournalpostResponse distribuerJournalpost(DistribuerJournalpostRequest request) {
        var rrequest = RestRequest.newPOSTJson(request, restConfig.endpoint(), restConfig);
        return restClient.send(rrequest, DistribuerJournalpostResponse.class);
    }

    public void distribuerJournalpost(JournalpostId journalpostId, BrevMottaker mottaker, Distribusjonstype distribusjonstype) {
        DistribuerJournalpostRequest request = new DistribuerJournalpostRequest(
            journalpostId.getVerdi(),
            UUID.randomUUID().toString(),
            getBestillendeFagsystem(),
            getDokumentProdAppKode(),
            distribusjonstype,
            Distribusjonstidspunkt.KJERNETID);
        DistribuerJournalpostResponse response = distribuerJournalpost(request);
        logger.info("Bestilt distribusjon av journalpost til {}, bestillingId ble {}", mottaker, response.bestillingsId());
    }

    private String getBestillendeFagsystem() {
        return switch (application) {
            case FPTILBAKE -> Fagsystem.FPSAK.getOffisiellKode();
            case K9TILBAKE -> Fagsystem.K9SAK.getOffisiellKode();
            default -> throw new IllegalArgumentException("Ikke-støttet applikasjon: " + application);
        };
    }

    private String getDokumentProdAppKode() {
        /* koder avtalt med team som eier dokdist */
        return switch (application) {
            case FPTILBAKE -> "FPTILBAKE";
            case K9TILBAKE -> "K9_TILBAKE";
            default -> throw new IllegalArgumentException("Ikke-støttet applikasjon: " + application);
        };
    }

}
