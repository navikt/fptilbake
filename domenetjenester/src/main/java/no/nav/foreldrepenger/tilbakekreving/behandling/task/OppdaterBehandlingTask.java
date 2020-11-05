package no.nav.foreldrepenger.tilbakekreving.behandling.task;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.hendelse.HendelseTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.util.env.Environment;

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
        if (Environment.current().isProd()) {
            //FIXME midlertidig unngå prosesstasken pga produksjonshendelse i Kafka.
            //FIXME denne skal fikses så snart som mulig
            return;
        }

        HendelseTaskDataWrapper dataWrapper = new HendelseTaskDataWrapper(taskData);
        dataWrapper.validerTaskDataOppdaterBehandling();

        Saksnummer saksnummer = dataWrapper.getSaksnummer();
        UUID eksternUuid = UUID.fromString(dataWrapper.getBehandlingUuid());
        Henvisning henvisning = dataWrapper.getHenvisning();

        behandlingTjeneste.oppdaterBehandlingMedEksternReferanse(saksnummer, henvisning, eksternUuid);
    }
}
