package no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste;

import java.util.List;
import java.util.Optional;

import no.nav.vedtak.felles.prosesstask.api.TaskStatus;

public interface GjenopptaBehandlingTjeneste {

    /**
     * Finner behandlinger som står på vent, som kan fortsettes automatisk.
     * @return
     */
    String automatiskGjenopptaBehandlinger();

    /**
     * Fortsetter behandling manuelt, registrerer brukerrespons hvis i varsel-steg
     * og venter på brukerrespons.
     * @param behandlingId
     * @return
     */
    Optional<String> fortsettBehandlingManuelt(long behandlingId);

    /**
     * Fortsetter behandling ved registrering av grunnlag dersom behandling er i TBKGSTEG.
     * @param behandlingId
     * @return
     */
    Optional<String> fortsettBehandlingMedGrunnlag(long behandlingId);

    /**
     * Fortsetter behandling, oppretter FortsettBehandlingTask
     * @param behandlingId
     * @return prosesstask gruppe ID
     */
    Optional<String> fortsettBehandling(long behandlingId);

    /**
     * Henter status for prosesstaskgruppe
     * @param gruppe
     * @return
     */
    List<TaskStatus> hentStatusForGjenopptaBehandlingGruppe(String gruppe);

    /**
     * Sjekk om behandling kan ta av vent
     * @param behandlingId
     * @return
     */
    boolean kanGjenopptaSteg(long behandlingId);

}
