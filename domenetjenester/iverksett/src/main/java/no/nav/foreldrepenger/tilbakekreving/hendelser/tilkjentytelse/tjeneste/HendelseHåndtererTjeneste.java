package no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.tjeneste;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.tilbakekrevingsvalg.VidereBehandling;
import no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.task.HendelseTaskDataWrapper;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ApplicationScoped
public class HendelseHåndtererTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(HendelseHåndtererTjeneste.class);

    private ProsessTaskRepository taskRepository;

    HendelseHåndtererTjeneste() {
        // CDI
    }

    @Inject
    public HendelseHåndtererTjeneste(ProsessTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public void håndterHendelse(HendelseTaskDataWrapper hendelseTaskDataWrapper) {
        long behandlingId = hendelseTaskDataWrapper.getBehandlingId();
        VidereBehandling videreBehandling = VidereBehandling.fraKode(hendelseTaskDataWrapper.getTilbakekrevingValg());

        if (VidereBehandling.TILBAKEKREV_I_INFOTRYGD.equals(videreBehandling)) {
            logger.info("Hendelse {} er relevant for tilbakekreving opprett for ekstern behandlingId={}", videreBehandling.getKode(), behandlingId);
            lagOpprettBehandlingTask(hendelseTaskDataWrapper);
        } else if (VidereBehandling.TILBAKEKR_OPPDATER.equals(videreBehandling)) {
            logger.info("Hendelse={} er relevant for å oppdatere eksistende tilbakekreving med ekstern behandlingId={}", videreBehandling.getKode(), behandlingId);
            lagOppdaterBehandlingTask(hendelseTaskDataWrapper);
        }
    }


    private void lagOpprettBehandlingTask(HendelseTaskDataWrapper hendelseTaskDataWrapper) {
        HendelseTaskDataWrapper taskData = HendelseTaskDataWrapper.lagWrapperForOpprettBehandling(hendelseTaskDataWrapper.getBehandlingUuid(),
            hendelseTaskDataWrapper.getBehandlingId(),
            hendelseTaskDataWrapper.getAktørId(),
            hendelseTaskDataWrapper.getSaksnummer());

        taskData.setFagsakYtelseType(hendelseTaskDataWrapper.getFagsakYtelseType());
        taskData.setBehandlingType(BehandlingType.TILBAKEKREVING);
        taskData.setVarselTekst(hendelseTaskDataWrapper.getVarselTekst());
        taskData.setVarselBeløp(hendelseTaskDataWrapper.getVarselBeløp());
        taskData.setTilbakekrevingValg(hendelseTaskDataWrapper.getTilbakekrevingValg());

        taskRepository.lagre(taskData.getProsessTaskData());
    }

    private void lagOppdaterBehandlingTask(HendelseTaskDataWrapper hendelseTaskDataWrapper) {
        HendelseTaskDataWrapper taskData = HendelseTaskDataWrapper.lagWrapperForOppdaterBehandling(hendelseTaskDataWrapper.getBehandlingUuid(),
            hendelseTaskDataWrapper.getBehandlingId(),
            hendelseTaskDataWrapper.getAktørId(),
            hendelseTaskDataWrapper.getSaksnummer());

        taskRepository.lagre(taskData.getProsessTaskData());
    }
}
