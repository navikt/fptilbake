package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskInfo;

/**
 * Task som utfører noe på en behandling, før prosessen kjøres videre.
 * Sikrer at behandlingslås task på riktig plass. Tasks som forsøker å kjøre behandling videre bør extende denne.
*/
public abstract class BehandlingskontrollProsessTask implements ProsessTaskHandler {

    private BehandlingRepository behandlingRepository;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    protected BehandlingskontrollProsessTask(BehandlingRepository behandlingRepository,
                                             BehandlingskontrollTjeneste behandlingskontrollTjeneste) {
        this.behandlingRepository = behandlingRepository;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Behandling behandling = finnBehandling(prosessTaskData);
        BehandlingskontrollKontekst kontekst = initBehandlingskontrollKontekst(behandling);

        prosesser(behandling);

        lagreBehandling(behandling, kontekst);
        kjørBehandlingskontroll(kontekst);
    }

    protected BehandlingskontrollKontekst initBehandlingskontrollKontekst(Behandling behandling) {
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
        return kontekst;
    }

    protected Behandling finnBehandling(ProsessTaskInfo prosessTaskData) {
        Long behandlingId = ProsessTaskDataWrapper.wrap(prosessTaskData).getBehandlingId();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        return behandling;
    }

    protected void kjørBehandlingskontroll(BehandlingskontrollKontekst kontekst) {
        // TODO (FC): Kall egen task for å kjøre videre?
        behandlingskontrollTjeneste.prosesserBehandling(kontekst);
    }

    protected void lagreBehandling(Behandling behandling, BehandlingskontrollKontekst kontekst) {
        behandlingRepository.lagre(behandling, kontekst.getSkriveLås());
    }

    protected abstract void prosesser(Behandling behandling);

}
