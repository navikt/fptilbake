package no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.tjeneste;

import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.tilbakekrevingsvalg.VidereBehandling;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.FagsystemKlient;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.TilbakekrevingValgDto;
import no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.task.HendelseTaskDataWrapper;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ApplicationScoped
public class HendelseHåndtererTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(HendelseHåndtererTjeneste.class);

    private ProsessTaskRepository taskRepository;
    private FagsystemKlient fagsystemKlient;

    HendelseHåndtererTjeneste() {
        // CDI
    }

    @Inject
    public HendelseHåndtererTjeneste(ProsessTaskRepository taskRepository, FagsystemKlient fagsystemKlient) {
        this.taskRepository = taskRepository;
        this.fagsystemKlient = fagsystemKlient;
    }

    public void håndterHendelse(HendelseTaskDataWrapper hendelseTaskDataWrapper) {
        Henvisning henvisning = hendelseTaskDataWrapper.getHenvisning();
        Optional<TilbakekrevingValgDto> tbkDataOpt = fagsystemKlient.hentTilbakekrevingValg(UUID.fromString(hendelseTaskDataWrapper.getBehandlingUuid()));

        if (tbkDataOpt.isPresent()) {
            TilbakekrevingValgDto tbkData = tbkDataOpt.get();
            if (erRelevantHendelseForOpprettTilbakekreving(tbkData)) {
                logger.info("Hendelse={} er relevant for tilbakekreving opprett for henvisning={}", tbkData.getVidereBehandling(), henvisning);
                lagOpprettBehandlingTask(hendelseTaskDataWrapper);
            } else if (erRelevantHendelseForOppdatereTilbakekreving(tbkData)) {
                logger.info("Hendelse={} er relevant for å oppdatere eksistende tilbakekreving med henvisning={}", tbkData.getVidereBehandling(), henvisning);
                lagOppdaterBehandlingTask(hendelseTaskDataWrapper);
            }
        }
    }

    private boolean erRelevantHendelseForOpprettTilbakekreving(TilbakekrevingValgDto tbkData) {
        //FIXME k9-tilbake k9-sak bruker en annen kode enn fpsak her
        return VidereBehandling.TILBAKEKREV_I_INFOTRYGD.equals(tbkData.getVidereBehandling());
    }

    private boolean erRelevantHendelseForOppdatereTilbakekreving(TilbakekrevingValgDto tbkData) {
        return VidereBehandling.TILBAKEKR_OPPDATER.equals(tbkData.getVidereBehandling());
    }

    private void lagOpprettBehandlingTask(HendelseTaskDataWrapper hendelseTaskDataWrapper) {
        HendelseTaskDataWrapper taskData = HendelseTaskDataWrapper.lagWrapperForOpprettBehandling(hendelseTaskDataWrapper.getBehandlingUuid(),
            hendelseTaskDataWrapper.getHenvisning(),
            hendelseTaskDataWrapper.getAktørId(),
            hendelseTaskDataWrapper.getSaksnummer());

        taskData.setFagsakYtelseType(hendelseTaskDataWrapper.getFagsakYtelseType());
        taskData.setBehandlingType(BehandlingType.TILBAKEKREVING);

        taskRepository.lagre(taskData.getProsessTaskData());
    }

    private void lagOppdaterBehandlingTask(HendelseTaskDataWrapper hendelseTaskDataWrapper) {
        HendelseTaskDataWrapper taskData = HendelseTaskDataWrapper.lagWrapperForOppdaterBehandling(hendelseTaskDataWrapper.getBehandlingUuid(),
            hendelseTaskDataWrapper.getHenvisning(),
            hendelseTaskDataWrapper.getAktørId(),
            hendelseTaskDataWrapper.getSaksnummer());

        taskRepository.lagre(taskData.getProsessTaskData());
    }
}
