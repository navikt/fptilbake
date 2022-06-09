package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dokdist;

import java.util.Objects;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @param journalpostId          Journalpost som skal distribueres
 * @param batchId                Identifiserer batch forsendelsen inngår i. Lar bestiller identifisere forsendelser som hører sammen. Fritekst, og konsument må selv vurdere hva som er hensiktsmessige verdier
 * @param bestillendeFagsystem   Fagsystemet som bestiller distribusjon
 * @param dokumentProdApp        Applikasjon som har produsert hoveddokumentet (for sporing og feilsøking)
 * @param distribusjonstype      Påvirker varsel tekst til bruker (mulig å velge fra: VEDTAK, VIKTIG, ANNET)
 * @param distribusjonstidspunkt Påvirker tidspunkt varsel sendes til bruker (mulig å velge fra: KJERNETID, UMIDDELBART)
 */
public record DistribuerJournalpostRequest(@NotNull String journalpostId,
                                           @NotNull String batchId,
                                           @NotNull String bestillendeFagsystem,
                                           @NotNull String dokumentProdApp,
                                           @JsonInclude(JsonInclude.Include.NON_NULL) Distribusjonstype distribusjonstype,
                                           @JsonInclude(JsonInclude.Include.NON_NULL) Distribusjonstidspunkt distribusjonstidspunkt) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String journalpostId;
        private String batchId;
        private String bestillendeFagsystem;
        private String dokumentProdApp;

        private Distribusjonstype distribusjonstype;

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

        public Builder medDokumentProdApp(String dokumentProdApp) {
            this.dokumentProdApp = dokumentProdApp;
            return this;
        }

        public Builder medDistribusjonstype(Distribusjonstype distribusjonstype) {
            this.distribusjonstype = distribusjonstype;
            return this;
        }

        public DistribuerJournalpostRequest build() {
            Objects.requireNonNull(journalpostId, "jounalpostId er påkrevd");
            Objects.requireNonNull(bestillendeFagsystem, "bestillendeFagsystem er påkrevd");
            Objects.requireNonNull(dokumentProdApp, "dokumentProdApp er påkrevd");

            return new DistribuerJournalpostRequest(
                journalpostId,
                batchId,
                bestillendeFagsystem,
                dokumentProdApp,
                distribusjonstype,
                distribusjonstype != null ? Distribusjonstidspunkt.KJERNETID : null);
        }
    }
}
