package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dokdist;


import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.journalpostapi.dto.sak.FagsakSystem;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;

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

    public void distribuerJournalpost(JournalpostId journalpostId, BrevMottaker mottaker) {
        DistribuerJournalpostRequest request = DistribuerJournalpostRequest.builder()
            .medJournalpostId(journalpostId.getVerdi())
            .medBestillendeFagsystem(getBestillendeFagsystem().getKode())
            .medDokumentProdApp(getDokumentProdAppKode())
            .build();
        DistribuerJournalpostResponse response = distribuerJournalpost(request);
        logger.info("Bestilt distribusjon av journalpost til {}, bestillingId ble {}", mottaker, response.getBestillingsId());
    }

    private FagsakSystem getBestillendeFagsystem() {
        return switch (applicationName) {
            case "fptilbake" -> FagsakSystem.FORELDREPENGELØSNINGEN;
            case "k9-tilbake" -> FagsakSystem.K9SAK;
            default -> throw new IllegalArgumentException("Ikke-støttet app.name: " + applicationName);
        };
    }

    private String getDokumentProdAppKode() {
        /* koder avtalt med team som eier dokdist */
        return switch (applicationName) {
            case "fptilbake" -> "FPTILBAKE";
            case "k9-tilbake" -> "K9_TILBAKE";
            default -> throw new IllegalArgumentException("Ikke-støttet app.name: " + applicationName);
        };
    }

}
