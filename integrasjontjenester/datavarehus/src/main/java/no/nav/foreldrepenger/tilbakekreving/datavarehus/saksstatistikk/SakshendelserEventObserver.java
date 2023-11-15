package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.AksjonspunktStatusEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.BehandlingEnhetEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.BehandlingStatusEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.BehandlingskontrollEvent;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping.BehandlingTilstandMapper;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.sakshendelse.BehandlingTilstand;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;


@ApplicationScoped
public class SakshendelserEventObserver {

    private ProsessTaskTjeneste taskTjeneste;
    private BehandlingTilstandTjeneste behandlingTilstandTjeneste;

    SakshendelserEventObserver() {
        // for CDI proxy
    }

    @Inject
    public SakshendelserEventObserver(ProsessTaskTjeneste taskTjeneste,
                                      BehandlingTilstandTjeneste behandlingTilstandTjeneste) {
        this.taskTjeneste = taskTjeneste;
        this.behandlingTilstandTjeneste = behandlingTilstandTjeneste;
    }

    public void observerAksjonpunktStatusEvent(@Observes AksjonspunktStatusEvent event) {
        klargjørSendingAvBehandlingensTilstand(event.getBehandlingId());
    }

    public void observerBehandlingAvsluttetEvent(@Observes BehandlingStatusEvent.BehandlingAvsluttetEvent event) {
        klargjørSendingAvBehandlingensTilstand(event.getBehandlingId());
    }

    public void observerAksjonspunktHarEndretBehandlendeEnhetEvent(@Observes BehandlingEnhetEvent event) {
        klargjørSendingAvBehandlingensTilstand(event.getBehandlingId());
    }

    public void observerStoppetEvent(@Observes BehandlingskontrollEvent.StoppetEvent event) {
        klargjørSendingAvBehandlingensTilstand(event.getBehandlingId());
    }

    private void klargjørSendingAvBehandlingensTilstand(long behandlingId) {
        BehandlingTilstand tilstand = behandlingTilstandTjeneste.hentBehandlingensTilstand(behandlingId);
        opprettProsessTask(behandlingId, tilstand);
    }

    private void opprettProsessTask(long behandlingId, BehandlingTilstand behandlingTilstand) {
        ProsessTaskData taskData = ProsessTaskData.forProsessTask(SendSakshendelserTilDvhTask.class);
        taskData.setPayload(BehandlingTilstandMapper.tilJsonString(behandlingTilstand));
        taskData.setProperty("behandlingId", Long.toString(behandlingId));

        taskTjeneste.lagre(taskData);
    }


}
