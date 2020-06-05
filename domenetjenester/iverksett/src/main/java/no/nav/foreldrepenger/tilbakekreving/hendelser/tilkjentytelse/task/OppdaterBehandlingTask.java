package no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.task;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(OppdaterBehandlingTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class OppdaterBehandlingTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "hendelser.oppdaterBehandling";

    private BehandlingTjeneste behandlingTjeneste;

    OppdaterBehandlingTask() {
        // for CDI proxy
    }

    @Inject
    public OppdaterBehandlingTask(BehandlingTjeneste behandlingTjeneste) {
        this.behandlingTjeneste = behandlingTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData taskData) {
        HendelseTaskDataWrapper dataWrapper = new HendelseTaskDataWrapper(taskData);
        dataWrapper.validerTaskDataOppdaterBehandling();

        Saksnummer saksnummer = dataWrapper.getSaksnummer();
        UUID eksternUuid = UUID.fromString(dataWrapper.getBehandlingUuid());
        Henvisning henvisning = dataWrapper.getHenvisning();

        behandlingTjeneste.oppdaterBehandlingMedEksternReferanse(saksnummer, henvisning, eksternUuid);
    }
}
