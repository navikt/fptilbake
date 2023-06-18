package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.task.FortsettBehandlingTask;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingVenterRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.log.mdc.MDCOperations;

@ApplicationScoped
public class GjenopptaBehandlingMedGrunnlagTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(GjenopptaBehandlingMedGrunnlagTjeneste.class);

    private ProsessTaskTjeneste taskTjeneste;
    private BehandlingVenterRepository behandlingVenterRepository;

    public GjenopptaBehandlingMedGrunnlagTjeneste() {
        // CDI
    }

    @Inject
    public GjenopptaBehandlingMedGrunnlagTjeneste(ProsessTaskTjeneste taskTjeneste,
                                                  BehandlingVenterRepository behandlingVenterRepository) {
        this.taskTjeneste = taskTjeneste;
        this.behandlingVenterRepository = behandlingVenterRepository;
    }

    /**
     * Fortsetter behandling ved registrering av grunnlag dersom behandling er i TBKGSTEG.
     *
     * @param behandlingId
     * @return
     */
    public void fortsettBehandlingMedGrunnlag(long behandlingId) {
        behandlingVenterRepository.hentBehandlingPåVent(behandlingId)
            .filter(behandling -> BehandlingStegType.TBKGSTEG.equals(behandling.getAktivtBehandlingSteg()) ||
                BehandlingStegType.FAKTA_FEILUTBETALING.equals(behandling.getAktivtBehandlingSteg()))
            .ifPresent(this::opprettFortsettBehandlingTask);
    }


    private String opprettFortsettBehandlingTask(Behandling behandling) {
        ProsessTaskData prosessTaskData = ProsessTaskData.forProsessTask(FortsettBehandlingTask.class);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskData.setSekvens("1");
        prosessTaskData.setPrioritet(100);
        prosessTaskData.setProperty(FortsettBehandlingTask.GJENOPPTA_STEG, behandling.getAktivtBehandlingSteg().getKode());

        var cid = MDCOperations.getCallId();
        var callId = (cid == null ? MDCOperations.generateCallId() : cid) + "_" + behandling.getId();
        prosessTaskData.setCallId(callId);

        logger.info("Gjenopptar behandling av behandlingId={}, oppretter {}-prosesstask med callId={}", behandling.getId(), prosessTaskData.getTaskType(), callId);
        return taskTjeneste.lagre(prosessTaskData);
    }


}
