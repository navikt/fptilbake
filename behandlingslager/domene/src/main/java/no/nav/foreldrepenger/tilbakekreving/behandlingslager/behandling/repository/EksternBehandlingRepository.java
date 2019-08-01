package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository;

import java.util.List;
import java.util.Optional;

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
     * Finner alle behandlinger som knyttet med behandlingId fra ekstern system
     * @param eksternBehandlingId
     */
    List<EksternBehandling> hentAlleBehandlingerMedEksternId(long eksternBehandlingId);

    /**
     * Finner siste avsluttet TilbakekkrevingBehandling basert på eksternBehandlingId
     * @param eksternBehandlingId
     * @return
     */
    Optional<EksternBehandling> finnForSisteAvsluttetTbkBehandling(long eksternBehandlingId);
}
