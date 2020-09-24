package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dokdist;


import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class DokdistKlient {

    private static final Logger logger = LoggerFactory.getLogger(DokdistKlient.class);

    private OidcRestClient oidcRestClient;
    private URI dokdistUri;

    DokdistKlient() {
        //for CDI proxy
    }

    @Inject
    public DokdistKlient(OidcRestClient oidcRestClient, @KonfigVerdi(value = "dokdist.rest.distribuer.journalpost") String dokdistUrl) {
        this.oidcRestClient = oidcRestClient;
        this.dokdistUri = UriBuilder.fromUri(dokdistUrl).build();
    }

    /**
     * Dokumentasjon: https://confluence.adeo.no/pages/viewpage.action?pageId=320039012
     */
    public DistribuerJournalpostResponse distribuerJournalpost(DistribuerJournalpostRequest request) {
        return oidcRestClient.post(dokdistUri, request, DistribuerJournalpostResponse.class);
    }

    public void distribuerJournalpost(JournalpostId journalpostId, BrevMottaker brevMottaker, Adresseinfo mottakerAdresse) {
        DistribuerJournalpostRequest.Builder request = DistribuerJournalpostRequest.builder()
            .medJournalpostId(journalpostId.getVerdi())
            .medBestillendeFagsystem("K9")
            .medDokumentProdApp(System.getProperty("application.name"));

        if (brevMottaker != BrevMottaker.BRUKER) {
            request.medAdresse(Adresse.builder()
                .medAdresselinje1(mottakerAdresse.getAdresselinje1())
                .medAdresselinje2(mottakerAdresse.getAdresselinje2())
                .medAdresselinje3(mottakerAdresse.getAdresselinje3())
                .medPostnummer(mottakerAdresse.getPostNr())
                .medPoststed(mottakerAdresse.getPoststed())
                .medLand(mottakerAdresse.getLand())
                .build());
        }

        DistribuerJournalpostResponse response = distribuerJournalpost(request.build());
        logger.info("Bestilt distribusjon av journalpost til {} for {] bestillingId ble {}", brevMottaker, response.getBestillingsId());
    }

}
