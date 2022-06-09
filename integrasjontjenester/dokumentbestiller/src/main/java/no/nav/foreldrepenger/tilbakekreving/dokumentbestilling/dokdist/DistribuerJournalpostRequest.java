package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dokdist;

import java.util.Objects;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;

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

    public DistribuerJournalpostRequest(String journalpostId,
                                        String batchId,
                                        String bestillendeFagsystem,
                                        String dokumentProdApp,
                                        Distribusjonstype distribusjonstype,
                                        Distribusjonstidspunkt distribusjonstidspunkt) {
        this.journalpostId = Objects.requireNonNull(journalpostId, "jounalpostId er påkrevd");
        this.bestillendeFagsystem = Objects.requireNonNull(bestillendeFagsystem, "bestillendeFagsystem er påkrevd");
        this.dokumentProdApp = Objects.requireNonNull(dokumentProdApp, "dokumentProdApp er påkrevd");
        this.batchId = batchId;
        this.distribusjonstype = distribusjonstype;
        this.distribusjonstidspunkt = distribusjonstidspunkt != null ? Distribusjonstidspunkt.KJERNETID : null;
    }
}
