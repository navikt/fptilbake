package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.AksjonspunktTilbakeførtEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.AksjonspunktUtførtEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.AksjonspunkterFunnetEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingEnhetEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStatusEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollEvent;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping.BehandlingTilstandMapper;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.sakshendelse.BehandlingTilstand;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.sakshendelse.DvhEventHendelse;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;


@ApplicationScoped
public class SakshendelserEventObserver {

    private static final Logger logger = LoggerFactory.getLogger(SakshendelserEventObserver.class);

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

    public void observerAksjonpunktFunnetEvent(@Observes AksjonspunkterFunnetEvent event) {
        klargjørSendingAvBehandlingensTilstand(event.getBehandlingId(), DvhEventHendelse.AKSJONSPUNKT_OPPRETTET);
    }

    public void observerAksjonpunktUtførtEvent(@Observes AksjonspunktUtførtEvent event) {
        klargjørSendingAvBehandlingensTilstand(event.getBehandlingId(), DvhEventHendelse.AKSJONSPUNKT_UTFØRT);
    }

    public void observerAksjonpunktTilbakeførtEvent(@Observes AksjonspunktTilbakeførtEvent event) {
        klargjørSendingAvBehandlingensTilstand(event.getBehandlingId(), DvhEventHendelse.AKSJONSPUNKT_TILBAKEFØR);
    }

    public void observerBehandlingAvsluttetEvent(@Observes BehandlingStatusEvent.BehandlingAvsluttetEvent event) {
        klargjørSendingAvBehandlingensTilstand(event.getBehandlingId(), DvhEventHendelse.AKSJONSPUNKT_AVBRUTT);
    }

    public void observerAksjonspunktHarEndretBehandlendeEnhetEvent(@Observes BehandlingEnhetEvent event) {
        klargjørSendingAvBehandlingensTilstand(event.getBehandlingId(), DvhEventHendelse.AKSJONSPUNKT_HAR_ENDRET_BEHANDLENDE_ENHET);
    }

    public void observerStoppetEvent(@Observes BehandlingskontrollEvent.StoppetEvent event) {
        klargjørSendingAvBehandlingensTilstand(event.getBehandlingId(), DvhEventHendelse.BEHANDLINGSKONTROLL_EVENT);
    }

    private void klargjørSendingAvBehandlingensTilstand(long behandlingId, DvhEventHendelse eventHendelse) {
        BehandlingTilstand tilstand = behandlingTilstandTjeneste.hentBehandlingensTilstand(behandlingId);
        opprettProsessTask(behandlingId, tilstand, eventHendelse);
    }

    private void opprettProsessTask(long behandlingId, BehandlingTilstand behandlingTilstand, DvhEventHendelse eventHendelse) {
        ProsessTaskData taskData = ProsessTaskData.forProsessTask(SendSakshendelserTilDvhTask.class);
        taskData.setPayload(BehandlingTilstandMapper.tilJsonString(behandlingTilstand));
        taskData.setProperty("behandlingId", Long.toString(behandlingId));
        taskData.setProperty("eventHendlese", eventHendelse.name());

        taskTjeneste.lagre(taskData);
    }


}
