package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;

public interface EksternBehandlingRepository {

    /**
     * Lagrer eksternBehandling, setter eksisterende, hvis finnes, til inaktiv
     * @param eksternBehandling
     */
    Long lagre(EksternBehandling eksternBehandling);

    /**
     * Henter eksternBehandling data for behandlingen
     * @param internBehandlingId
     */
    EksternBehandling hentFraInternId(long internBehandlingId);

    /**
     * Finner eksternBehandling med behandlingId fra eksternt system
     * @param eksternBehandlingId
     */
    Optional<EksternBehandling> hentFraEksternId(long eksternBehandlingId);

    /**
     * Finner alle behandlinger som knyttet med uuid fra ekstern system
     * @param eksternUuid
     */
    List<EksternBehandling> hentAlleBehandlingerMedEksternUuid(UUID eksternUuid);

    /**
     * Finner siste avsluttet TilbakekkrevingBehandling basert på eksternUuid
     * @param eksternUuid
     * @return
     */
    Optional<EksternBehandling> finnForSisteAvsluttetTbkBehandling(UUID eksternUuid);
}
