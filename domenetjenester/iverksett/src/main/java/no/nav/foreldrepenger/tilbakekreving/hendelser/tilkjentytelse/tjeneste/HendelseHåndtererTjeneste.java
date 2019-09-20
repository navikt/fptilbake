package no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.tjeneste;

import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.tilbakekrevingsvalg.VidereBehandling;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.FpsakKlient;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.TilbakekrevingValgDto;
import no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.task.HendelseTaskDataWrapper;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ApplicationScoped
public class HendelseHåndtererTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(HendelseHåndtererTjeneste.class);

    private ProsessTaskRepository taskRepository;
    private FpsakKlient restKlient;

    HendelseHåndtererTjeneste() {
        // CDI
    }

    @Inject
    public HendelseHåndtererTjeneste(ProsessTaskRepository taskRepository, FpsakKlient restKlient) {
        this.taskRepository = taskRepository;
        this.restKlient = restKlient;
    }

    public void håndterHendelse(HendelseTaskDataWrapper hendelseTaskDataWrapper) {
        long behandlingId = hendelseTaskDataWrapper.getBehandlingId();
        Optional<TilbakekrevingValgDto> tbkDataOpt = restKlient.hentTilbakekrevingValg(UUID.fromString(hendelseTaskDataWrapper.getBehandlingUuid()));

        if (tbkDataOpt.isPresent() && erRelevantHendelse(tbkDataOpt.get())) {
            logger.info("Registrert ny relevant hendelse for ekstern behandlingId={}", behandlingId);
            lagOpprettBehandlingTask(hendelseTaskDataWrapper);
        }
    }

    private boolean erRelevantHendelse(TilbakekrevingValgDto tbkData) {
        return VidereBehandling.TILBAKEKREV_I_INFOTRYGD.equals(tbkData.getVidereBehandling());
    }

    private void lagOpprettBehandlingTask(HendelseTaskDataWrapper hendelseTaskDataWrapper) {
        HendelseTaskDataWrapper taskData = HendelseTaskDataWrapper.lagWrapperForOpprettBehandling(hendelseTaskDataWrapper.getBehandlingUuid(),
            hendelseTaskDataWrapper.getBehandlingId(),
            hendelseTaskDataWrapper.getAktørId());

        taskData.setSaksnummer(hendelseTaskDataWrapper.getSaksnummer());
        taskData.setFagsakYtelseType(hendelseTaskDataWrapper.getFagsakYtelseType());
        taskData.setBehandlingType(BehandlingType.TILBAKEKREVING);

        taskRepository.lagre(taskData.getProsessTaskData());
    }
}
