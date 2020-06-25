package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingslagerRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;

public interface BehandlingRepository extends BehandlingslagerRepository {

    /**
     * Hent Behandling med angitt id.
     */
    Behandling hentBehandling(Long behandlingId);

    /**
     * Hent Behandling med angitt UUid.
     */
    Behandling hentBehandling(UUID uuid);

    /**
     * Lagrer behandling, sikrer at relevante parent-entiteter (Fagsak, FagsakRelasjon) også oppdateres.
     */
    Long lagre(Behandling behandling, BehandlingLås lås);

    /**
     * Lagrer behandling, og renser first-level cache i JPA
     */
    Long lagreOgClear(Behandling behandling, BehandlingLås lås);

    /**
     * Sjekker om versjonen på behandlingen har blitt endret
     */
    Boolean erVersjonUendret(Long behandlingId, Long versjon);

    /**
     * Lager en ny Behandling basert på en gammel, med samme grunnlag strukturer.
     */
    Behandling opprettNyBehandlingBasertPåTidligere(Behandling gammelBehandling, BehandlingType behandlingType);

    /**
     * Ta lås for oppdatering av behandling/fagsak. Påkrevd før lagring.
     */
    BehandlingLås taSkriveLås(Behandling behandling);

    /**
     * Hent siste behandling for angitt {@link Fagsak#id} og behandling type
     */
    Optional<Behandling> hentSisteBehandlingForFagsakId(Long fagsakId, BehandlingType behandlingType);

    // sjekk lås og oppgrader til skriv
    void verifiserBehandlingLås(BehandlingLås lås);

    BehandlingStegType finnBehandlingStegType(String kode);

    /**
     * Hent alle behandlinger som ikke er avsluttet på fagsak.
     */
    List<Behandling> hentBehandlingerSomIkkeErAvsluttetForFagsakId(Long fagsakId);

    List<Behandling> hentAlleBehandlingerForSaksnummer(Saksnummer saksnummer);

    String hentSaksnummerForBehandling(long behandlingId);

    List<Behandling> hentAlleBehandlinger();

    List<Behandling> hentAlleAvsluttetBehandlinger();

}
