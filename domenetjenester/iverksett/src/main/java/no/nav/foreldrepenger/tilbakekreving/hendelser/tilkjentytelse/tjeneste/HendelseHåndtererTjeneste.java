package no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.tjeneste;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.FpsakKlient;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.TilbakekrevingDataDto;
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

    public void håndterHendelse(long fagsakId, long behandlingId, String aktørId) {
        Optional<TilbakekrevingDataDto> tbkDataOpt = restKlient.hentTilbakekrevingData(behandlingId);

        if (tbkDataOpt.isPresent() && erRelevantHendelse(tbkDataOpt.get())) {
            logger.info("Registrert ny relevant hendelse for ekstern behandlingId={}", behandlingId);
            lagOpprettBehandlingTask(fagsakId, behandlingId, aktørId, tbkDataOpt.get());
        }
    }

    private boolean erRelevantHendelse(TilbakekrevingDataDto tbkData) {
        return VidereBehandling.TILBAKEKREV_I_INFOTRYGD.getKode().equals(tbkData.getVidereBehandling());
    }

    private void lagOpprettBehandlingTask(long fagsakId, long behandlingId, String aktørId, TilbakekrevingDataDto tbkData) {
        HendelseTaskDataWrapper taskData = HendelseTaskDataWrapper.lagWrapperForOpprettBehandling(fagsakId, behandlingId, aktørId);
        taskData.setSaksnummer(tbkData.getSaksnummer());
        taskData.setFagsakYtelseType(tbkData.getFagsakYtelseType());
        taskData.setBehandlingType(BehandlingType.TILBAKEKREVING.getKode());

        taskRepository.lagre(taskData.getProsessTaskData());
    }
}
