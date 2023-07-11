package no.nav.foreldrepenger.tilbakekreving.behandling.task;

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(value = "hendelser.opprettBehandling", maxFailedRuns = 5)
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class OpprettBehandlingTask implements ProsessTaskHandler {

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

        var eksternBehandlingUuid = dataWrapper.getBehandlingUuid();
        Henvisning henvisning = dataWrapper.getHenvisning();
        Saksnummer saksnummer = dataWrapper.getSaksnummer();
        AktørId aktørId = dataWrapper.getAktørId();
        BehandlingType behandlingType = dataWrapper.getBehandlingType();
        FagsakYtelseType fagsakYtelseType = dataWrapper.getFagsakYtelseType();

        opprettBehandling(saksnummer, eksternBehandlingUuid, henvisning, aktørId, fagsakYtelseType, behandlingType);
    }

    private void opprettBehandling(Saksnummer saksnummer, UUID eksternUuid, Henvisning henvisning, AktørId aktørId, FagsakYtelseType fagsakYtelseType, BehandlingType behandlingType) {
        behandlingTjeneste.opprettBehandlingAutomatisk(saksnummer, eksternUuid, henvisning, aktørId, fagsakYtelseType, behandlingType);
    }

}
