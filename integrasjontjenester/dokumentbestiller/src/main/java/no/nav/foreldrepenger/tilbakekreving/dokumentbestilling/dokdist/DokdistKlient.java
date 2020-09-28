package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dokdist;


import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.journalpostapi.dto.sak.FagsakSystem;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class DokdistKlient {

    private static final Logger logger = LoggerFactory.getLogger(DokdistKlient.class);

    private OidcRestClient oidcRestClient;
    private URI dokdistUri;
    private String applicationName;

    DokdistKlient() {
        //for CDI proxy
    }

    @Inject
    public DokdistKlient(OidcRestClient oidcRestClient, @KonfigVerdi(value = "dokdist.rest.distribuer.journalpost") String dokdistUrl, @KonfigVerdi("app.name") String applicationName) {
        this.oidcRestClient = oidcRestClient;
        this.dokdistUri = UriBuilder.fromUri(dokdistUrl).build();
        this.applicationName = applicationName;
    }

    /**
     * Dokumentasjon: https://confluence.adeo.no/pages/viewpage.action?pageId=320039012
     */
    public DistribuerJournalpostResponse distribuerJournalpost(DistribuerJournalpostRequest request) {
        return oidcRestClient.post(dokdistUri, request, DistribuerJournalpostResponse.class);
    }

    public void distribuerJournalpostTilBruker(String journalpostId) {
        DistribuerJournalpostRequest request = DistribuerJournalpostRequest.builder()
            .medJournalpostId(journalpostId)
            .medBestillendeFagsystem(getBestillendeFagsystem().getKode())
            .medDokumentProdApp(applicationName)
            .build();
        DistribuerJournalpostResponse response = distribuerJournalpost(request);
        logger.info("Bestilt distribusjon av journalpost til bruker, bestillingId ble {}", response.getBestillingsId());
    }

    public void distribuerJournalpostTilVerge(String journalpostId, Adresse vergeAdresse) {
        DistribuerJournalpostRequest request = DistribuerJournalpostRequest.builder()
            .medJournalpostId(journalpostId)
            .medBestillendeFagsystem(getBestillendeFagsystem().getKode())
            .medDokumentProdApp(applicationName)
            .medAdresse(vergeAdresse)
            .build();
        DistribuerJournalpostResponse response = distribuerJournalpost(request);
        logger.info("Bestilt distribusjon av journalpost til Verge, bestillingId ble {}", response.getBestillingsId());
    }

    private FagsakSystem getBestillendeFagsystem() {
        switch (applicationName) {
            case "fptilbake":
                return FagsakSystem.FORELDREPENGELØSNINGEN;
            case "k9-tilbake":
                return FagsakSystem.K9SAK;
            default:
                throw new IllegalArgumentException("Ikke-støttet app.name: " + applicationName);
        }
    }

}
