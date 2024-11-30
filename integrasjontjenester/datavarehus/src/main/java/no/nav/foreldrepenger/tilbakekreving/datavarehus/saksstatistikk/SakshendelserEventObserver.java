package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.AksjonspunktStatusEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.BehandlingEnhetEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.BehandlingSaksbehandlerEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.BehandlingStatusEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
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
        var aksjonspunkter = event.getAksjonspunkter();
        // Utvider behandlingStatus i DVH med VenteKategori
        if (aksjonspunkter.stream().anyMatch(Aksjonspunkt::erAutopunkt)) {
            klargjørSendingAvBehandlingensTilstand(event);
        }
    }

    public void observerBehandlingStatusEvent(@Observes BehandlingStatusEvent event) {
        klargjørSendingAvBehandlingensTilstand(event);
    }

    public void observerEndretBehandlendeEnhetEvent(@Observes BehandlingEnhetEvent event) {
        klargjørSendingAvBehandlingensTilstand(event);
    }

    public void observerEndretAnsvarligSaksbehandlerEvent(@Observes BehandlingSaksbehandlerEvent event) {
        klargjørSendingAvBehandlingensTilstand(event);
    }

    private void klargjørSendingAvBehandlingensTilstand(BehandlingEvent event) {
        var tilstand = behandlingTilstandTjeneste.hentBehandlingensTilstand(event.getBehandlingId());
        if (tilstand != null) {
            opprettProsessTask(event, tilstand);
        }
    }

    private void opprettProsessTask(BehandlingEvent event, BehandlingTilstand behandlingTilstand) {
        ProsessTaskData taskData = ProsessTaskData.forProsessTask(SendSakshendelserTilDvhTask.class);
        taskData.setPayload(BehandlingTilstandMapper.tilJsonString(behandlingTilstand));
        taskData.setBehandling(event.getSaksnummer().getVerdi(), event.getFagsakId(), event.getBehandlingId());
        taskTjeneste.lagre(taskData);
    }


}
