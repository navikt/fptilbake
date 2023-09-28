package no.nav.foreldrepenger.tilbakekreving.los.klient.observer;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.AksjonspunktStatusEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.BehandlingEnhetEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.BehandlingStatusEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.BehandlingskontrollEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.foreldrepenger.tilbakekreving.los.klient.task.FpLosPubliserEventTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.hendelser.behandling.Hendelse;

@ApplicationScoped
public class FpLosEventObserver {

    private static final Logger LOG = LoggerFactory.getLogger(FpLosEventObserver.class);

    private Fagsystem fagsystem;

    private ProsessTaskTjeneste taskTjeneste;

    public FpLosEventObserver() {
    }

    @Inject
    public FpLosEventObserver(ProsessTaskTjeneste taskTjeneste) {
        this(taskTjeneste, ApplicationName.hvilkenTilbake());
    }

    public FpLosEventObserver(ProsessTaskTjeneste taskTjeneste,
                              Fagsystem fagsystem) {
        this.taskTjeneste = taskTjeneste;
        this.fagsystem = fagsystem;
    }

    public void observerStoppetEvent(@Observes BehandlingskontrollEvent.StoppetEvent event) {
        opprettProsessTask(event, Hendelse.AKSJONSPUNKT);
    }

    // Lytter på AksjonspunkterFunnetEvent, filtrer ut når behandling er satt manuelt på vent og legger melding på kafka
    public void observerAksjonpunktStatusEvent(@Observes AksjonspunktStatusEvent event) {
        var sattPåVentAvSaksbehandler = event.getAksjonspunkter().stream().anyMatch(FpLosEventObserver::manueltPåVent);
        if (sattPåVentAvSaksbehandler) {
            opprettProsessTask(event, Hendelse.VENTETILSTAND);
        }
    }

    private static boolean manueltPåVent(Aksjonspunkt aksjonspunkt) {
        var endretOpprettetAv = Optional.ofNullable(aksjonspunkt.getEndretAv()).orElseGet(aksjonspunkt::getOpprettetAv);
        return aksjonspunkt.erOpprettet() && aksjonspunkt.erAutopunkt() && !endretOpprettetAv.startsWith("srv");
    }

    public void observerBehandlingAvsluttetEvent(@Observes BehandlingStatusEvent.BehandlingOpprettetEvent event) {
        opprettProsessTask(event, Hendelse.OPPRETTET);
    }

    public void observerBehandlingAvsluttetEvent(@Observes BehandlingStatusEvent.BehandlingAvsluttetEvent event) {
        opprettProsessTask(event, Hendelse.AVSLUTTET);
    }

    public void observerAksjonspunktHarEndretBehandlendeEnhetEvent(@Observes BehandlingEnhetEvent event) {
        opprettProsessTask(event, Hendelse.ENHET);
    }

    private void opprettProsessTask(BehandlingEvent behandlingEvent, Hendelse hendelse) {
        if (Fagsystem.FPTILBAKE.equals(fagsystem)) {
            var prosessTaskData = ProsessTaskData.forProsessTask(FpLosPubliserEventTask.class);
            prosessTaskData.setBehandling(behandlingEvent.getFagsakId(), behandlingEvent.getBehandlingId(), behandlingEvent.getAktørId().getId());
            prosessTaskData.setProperty(FpLosPubliserEventTask.PROPERTY_EVENT_NAME, hendelse.name());
            prosessTaskData.setCallIdFraEksisterende();
            prosessTaskData.setPrioritet(90);
            taskTjeneste.lagre(prosessTaskData);
        }
    }
}
