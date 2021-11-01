package no.nav.foreldrepenger.tilbakekreving.selvbetjening.klient.task;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.selvbetjening.klient.SelvbetjeningTjeneste;
import no.nav.foreldrepenger.tilbakekreving.selvbetjening.klient.dto.Hendelse;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;


@ApplicationScoped
@ProsessTask("send.beskjed.tilbakekreving.henlagt.selvbetjening")
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class SendTilbakekrevingHenlagtTilSelvbetjeningTask implements ProsessTaskHandler {

    private SelvbetjeningTjeneste selvbetjeningTjeneste;

    SendTilbakekrevingHenlagtTilSelvbetjeningTask() {
        //for CDI proxy
    }

    @Inject
    public SendTilbakekrevingHenlagtTilSelvbetjeningTask(SelvbetjeningTjeneste selvbetjeningTjeneste) {
        this.selvbetjeningTjeneste = selvbetjeningTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingId = ProsessTaskDataWrapper.wrap(prosessTaskData).getBehandlingId();
        selvbetjeningTjeneste.sendMelding(behandlingId, Hendelse.TILBAKEKREVING_HENLAGT);
    }
}
