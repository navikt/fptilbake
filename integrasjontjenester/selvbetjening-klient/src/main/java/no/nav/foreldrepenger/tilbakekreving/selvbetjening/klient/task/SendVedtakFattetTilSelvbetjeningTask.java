package no.nav.foreldrepenger.tilbakekreving.selvbetjening.klient.task;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.selvbetjening.klient.SelvbetjeningTjeneste;
import no.nav.foreldrepenger.tilbakekreving.selvbetjening.klient.dto.Hendelse;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;


@ApplicationScoped
@ProsessTask("send.beskjed.vedtak.fattet.selvbetjening")
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class SendVedtakFattetTilSelvbetjeningTask implements ProsessTaskHandler {

    private SelvbetjeningTjeneste selvbetjeningTjeneste;

    SendVedtakFattetTilSelvbetjeningTask() {
        //for CDI proxy
    }

    @Inject
    public SendVedtakFattetTilSelvbetjeningTask(SelvbetjeningTjeneste selvbetjeningTjeneste) {
        this.selvbetjeningTjeneste = selvbetjeningTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var behandlingId = ProsessTaskDataWrapper.wrap(prosessTaskData).getBehandlingId();
        selvbetjeningTjeneste.sendMelding(behandlingId, Hendelse.TILBAKEKREVING_FATTET_VEDTAK);
    }
}
