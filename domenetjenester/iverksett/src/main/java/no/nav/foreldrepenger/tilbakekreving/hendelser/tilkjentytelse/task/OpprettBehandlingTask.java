package no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.task;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

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
        HendelseTaskDataWrapper dataWrapper = new HendelseTaskDataWrapper(taskData);
        dataWrapper.validerTaskDataOpprettBehandling();

        long eksternFagsakId = dataWrapper.getFagsakId();
        long eksternBehandlingId = dataWrapper.getBehandlingId();
        Saksnummer saksnummer = dataWrapper.getSaksnummer();
        AktørId aktørId = dataWrapper.getAktørId();
        BehandlingType behandlingType = dataWrapper.getBehandlingType();
        FagsakYtelseType fagsakYtelseType = dataWrapper.getFagsakYtelseType();

        opprettBehandling(saksnummer, eksternFagsakId, eksternBehandlingId, aktørId, fagsakYtelseType, behandlingType);
    }

    private void opprettBehandling(Saksnummer saksnummer, long fagsakId, long eksternBehandlingId,
                                   AktørId aktørId, FagsakYtelseType fagsakYtelseType,
                                   BehandlingType behandlingType) {
        behandlingTjeneste.opprettBehandlingAutomatisk(saksnummer, fagsakId, eksternBehandlingId, aktørId, fagsakYtelseType, behandlingType);
    }
}
