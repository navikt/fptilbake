package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import java.util.Optional;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.task.FortsettBehandlingTask;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.log.mdc.MDCOperations;

@ApplicationScoped
public class GjenopptaBehandlingMedGrunnlagTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(GjenopptaBehandlingMedGrunnlagTjeneste.class);
    private static final Set<BehandlingStegType> FORTSETT = Set.of(BehandlingStegType.TBKGSTEG, BehandlingStegType.FAKTA_FEILUTBETALING);

    private ProsessTaskTjeneste taskTjeneste;

    public GjenopptaBehandlingMedGrunnlagTjeneste() {
        // CDI
    }

    @Inject
    public GjenopptaBehandlingMedGrunnlagTjeneste(ProsessTaskTjeneste taskTjeneste) {
        this.taskTjeneste = taskTjeneste;
    }

    /**
     * Fortsetter behandling ved registrering av grunnlag dersom behandling er i TBKGSTEG.
     *
     * @param behandling
     * @return
     */
    public void fortsettBehandlingMedGrunnlag(Behandling behandling) {
        fortsettBehandlingMedGrunnlag(behandling, false);
    }

    public void fortsettBehandlingMedGrunnlag(Behandling behandling, boolean skalFortsette) {
        var reellSkalFortsette = skalFortsette || (behandling.isBehandlingPåVent() && FORTSETT.contains(behandling.getAktivtBehandlingSteg()));
        if (reellSkalFortsette) {
            opprettFortsettBehandlingTask(behandling);
        }
    }


    private void opprettFortsettBehandlingTask(Behandling behandling) {
        ProsessTaskData prosessTaskData = ProsessTaskData.forProsessTask(FortsettBehandlingTask.class);
        prosessTaskData.setBehandling(behandling.getSaksnummer().getVerdi(), behandling.getFagsakId(), behandling.getId());
        prosessTaskData.setSekvens("1");
        prosessTaskData.setPrioritet(3);
        prosessTaskData.setProperty(FortsettBehandlingTask.GJENOPPTA_STEG, behandling.getAktivtBehandlingSteg().getKode());

        var callId = Optional.ofNullable(MDCOperations.getCallId()).orElseGet(MDCOperations::generateCallId);
        prosessTaskData.setCallId(callId +  "_" + behandling.getId());

        LOG.info("Gjenopptar behandling av behandlingId={}, oppretter {} med callId={}", behandling.getId(), prosessTaskData.taskType(), callId);
        taskTjeneste.lagre(prosessTaskData);
    }


}
