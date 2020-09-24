package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dokdist;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DistribuerJournalpostResponse {
    @JsonProperty("bestillingsId")
    private String bestillingsId;

    public String getBestillingsId() {
        return bestillingsId;
    }

    public void setBestillingsId(String bestillingsId) {
        this.bestillingsId = bestillingsId;
    }
}
