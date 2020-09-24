package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dokdist;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DistribuerJournalpostRequest {

    /**
     * Journalpost som skal distribueres
     */
    @JsonProperty("journalpostId")
    private String journalpostId;

    /**
     * Identifiserer batch forsendelsen inngår i. Lar bestiller identifisere forsendelser som hører sammen. Fritekst, og konsument må selv vurdere hva som er hensiktsmessige verdier
     */
    @JsonProperty("batchId")
    private String batchId;

    /**
     * Fagsystemet som bestiller distribusjon
     */
    @JsonProperty("bestillendeFagssystem")
    private String bestillendeFagsystem;

    @JsonProperty("adresse")
    private Adresse adresse;

    /**
     * Applikasjon som har produsert hoveddokumentet (for sporing og feilsøking
     */
    @JsonProperty("dokumentProdApp")
    private String dokumentProdApp;

    public String getJournalpostId() {
        return journalpostId;
    }

    public String getBatchId() {
        return batchId;
    }

    public String getBestillendeFagsystem() {
        return bestillendeFagsystem;
    }

    public String getDokumentProdApp() {
        return dokumentProdApp;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String journalpostId;
        private String batchId;
        private String bestillendeFagsystem;
        private Adresse adresse;
        private String dokumentProdApp;

        private Builder() {
        }

        public Builder medJournalpostId(String journalpostId) {
            this.journalpostId = journalpostId;
            return this;
        }

        public Builder medBatchId(String batchId) {
            this.batchId = batchId;
            return this;
        }

        public Builder medBestillendeFagsystem(String bestillendeFagsystem) {
            this.bestillendeFagsystem = bestillendeFagsystem;
            return this;
        }

        public Builder medAdresse(Adresse adresse) {
            this.adresse = adresse;
            return this;
        }

        public Builder medDokumentProdApp(String dokumentProdApp) {
            this.dokumentProdApp = dokumentProdApp;
            return this;
        }

        public DistribuerJournalpostRequest build() {
            Objects.requireNonNull(journalpostId, "jounalpostId er påkrevd");
            Objects.requireNonNull(bestillendeFagsystem, "bestillendeFagsystem er påkrevd");
            Objects.requireNonNull(dokumentProdApp, "dokumentProdApp er påkrevd");


            DistribuerJournalpostRequest request = new DistribuerJournalpostRequest();
            request.journalpostId = journalpostId;
            request.batchId = batchId;
            request.bestillendeFagsystem = bestillendeFagsystem;
            request.adresse = adresse;
            request.dokumentProdApp = dokumentProdApp;
            return request;
        }
    }
}
