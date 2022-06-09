package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dokdist;


import java.net.URI;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.journalpostapi.dto.sak.FagsakSystem;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;

@ApplicationScoped
public class DokdistKlient {

    private static final Logger logger = LoggerFactory.getLogger(DokdistKlient.class);

    private OidcRestClient oidcRestClient;

    private Fagsystem application;

    DokdistKlient() {
        //for CDI proxy
    }

    @Inject
    public DokdistKlient(OidcRestClient oidcRestClient) {
        this.oidcRestClient = oidcRestClient;
        this.application = ApplicationName.hvilkenTilbake();
    }

    /**
     * Dokumentasjon: https://confluence.adeo.no/pages/viewpage.action?pageId=320039012
     */
    public DistribuerJournalpostResponse distribuerJournalpost(DistribuerJournalpostRequest request) {
        return oidcRestClient.post(baseUri(), request, DistribuerJournalpostResponse.class);
    }

    public void distribuerJournalpost(JournalpostId journalpostId, BrevMottaker mottaker, Distribusjonstype distribusjonstype) {
        DistribuerJournalpostRequest request = DistribuerJournalpostRequest.builder()
                .medJournalpostId(journalpostId.getVerdi())
                .medBatchId(UUID.randomUUID().toString())
                .medBestillendeFagsystem(getBestillendeFagsystem().getKode())
                .medDokumentProdApp(getDokumentProdAppKode())
                .medDistribusjonstype(distribusjonstype)
                .build();
        DistribuerJournalpostResponse response = distribuerJournalpost(request);
        logger.info("Bestilt distribusjon av journalpost til {}, bestillingId ble {}", mottaker, response.bestillingsId());
    }

    private FagsakSystem getBestillendeFagsystem() {
        return switch (application) {
            case FPTILBAKE -> FagsakSystem.FORELDREPENGELØSNINGEN;
            case K9TILBAKE -> FagsakSystem.K9SAK;
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

    private URI baseUri() {
        return Environment.current().getProperty("dokdist.rest.distribuer.journalpost", URI.class);
    }

}
