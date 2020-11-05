package no.nav.foreldrepenger.tilbakekreving.behandling.task;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.hendelse.HendelseTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.util.env.Environment;

@ApplicationScoped
@ProsessTask(OpprettBehandlingTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class OpprettBehandlingTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "hendelser.opprettBehandling";

    BehandlingTjeneste behandlingTjeneste;

    OpprettBehandlingTask() {
        // CDI
    }

    @Inject
    public OpprettBehandlingTask(BehandlingTjeneste behandlingTjeneste) {
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
        dataWrapper.validerTaskDataOpprettBehandling();

        String eksternBehandlingUuid = dataWrapper.getBehandlingUuid();
        Henvisning henvisning = dataWrapper.getHenvisning();
        Saksnummer saksnummer = dataWrapper.getSaksnummer();
        AktørId aktørId = dataWrapper.getAktørId();
        BehandlingType behandlingType = dataWrapper.getBehandlingType();
        FagsakYtelseType fagsakYtelseType = dataWrapper.getFagsakYtelseType();

        opprettBehandling(saksnummer, UUID.fromString(eksternBehandlingUuid), henvisning, aktørId, fagsakYtelseType, behandlingType);
    }

    private void opprettBehandling(Saksnummer saksnummer, UUID eksternUuid, Henvisning henvisning, AktørId aktørId, FagsakYtelseType fagsakYtelseType, BehandlingType behandlingType) {
        behandlingTjeneste.opprettBehandlingAutomatisk(saksnummer, eksternUuid, henvisning, aktørId, fagsakYtelseType, behandlingType);
    }

}
