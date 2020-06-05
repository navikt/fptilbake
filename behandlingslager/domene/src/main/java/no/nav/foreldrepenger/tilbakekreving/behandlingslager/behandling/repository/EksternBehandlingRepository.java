package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;

public interface EksternBehandlingRepository {

    /**
     * Lagrer eksternBehandling, setter eksisterende, hvis finnes, til inaktiv
     * @param eksternBehandling
     */
    void lagre(EksternBehandling eksternBehandling);

    /**
     * Henter eksternBehandling data for behandlingen
     * @param internBehandlingId
     */
    EksternBehandling hentFraInternId(long internBehandlingId);

    Optional<EksternBehandling> hentFraHenvisning(Henvisning henvisning);

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

    boolean finnesEksternBehandling(long internId, Henvisning henvisning);

    void deaktivateTilkobling(long internId);

    /**
     * Finner eksternbehandling for siste aktivert internId
     * @param internBehandlingId
     * @return eksternBehandling
     */
    EksternBehandling hentForSisteAktivertInternId(long internBehandlingId);

    Optional<EksternBehandling> hentOptionalFraInternId(long internBehandlingId);

}
