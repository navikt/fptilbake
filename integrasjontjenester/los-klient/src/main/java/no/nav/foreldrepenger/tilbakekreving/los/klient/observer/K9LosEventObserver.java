package no.nav.foreldrepenger.tilbakekreving.los.klient.observer;

import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType.FAKTA_FEILUTBETALING;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.AksjonspunktStatusEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.BehandlingEnhetEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.BehandlingStatusEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.BehandlingskontrollEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.foreldrepenger.tilbakekreving.los.klient.task.K9LosPubliserEventTask;
import no.nav.vedtak.felles.integrasjon.kafka.EventHendelse;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;


@ApplicationScoped
public class K9LosEventObserver {

    private static final Logger LOG = LoggerFactory.getLogger(K9LosEventObserver.class);
    private static final String LOGGER_OPPRETTER_PROSESS_TASK = "Oppretter prosess task for å publisere event={} til los for aksjonspunkt={}";

    private Fagsystem fagsystem;

    private ProsessTaskTjeneste taskTjeneste;
    private BehandlingRepository behandlingRepository;

    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    K9LosEventObserver() {
        // for CDI proxy
    }

    @Inject
    public K9LosEventObserver(BehandlingRepository behandlingRepository,
                              ProsessTaskTjeneste taskTjeneste,
                              BehandlingskontrollTjeneste behandlingskontrollTjeneste) {
        this(behandlingRepository, taskTjeneste, behandlingskontrollTjeneste, ApplicationName.hvilkenTilbake());
    }

    public K9LosEventObserver(BehandlingRepository behandlingRepository,
                              ProsessTaskTjeneste taskTjeneste,
                              BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                              Fagsystem fagsystem) {
        this.behandlingRepository = behandlingRepository;
        this.taskTjeneste = taskTjeneste;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.fagsystem = fagsystem;
    }

    public void observerAksjonpunktStatusEvent(@Observes AksjonspunktStatusEvent event) {
        if (!Fagsystem.FPTILBAKE.equals(fagsystem)) {
            Long behandlingId = event.getBehandlingId();
            for (Aksjonspunkt aksjonspunkt : event.getAksjonspunkter()) {
                if (AksjonspunktStatus.OPPRETTET.equals(aksjonspunkt.getStatus()) && (aksjonspunkt.erManuell() || erBehandlingIFaktaEllerSenereSteg(
                    behandlingId))) {
                    LOG.info(LOGGER_OPPRETTER_PROSESS_TASK, EventHendelse.AKSJONSPUNKT_OPPRETTET,
                        aksjonspunkt.getAksjonspunktDefinisjon().getKode());
                    opprettProsessTask(event.getFagsakId(), behandlingId, event.getAktørId(), EventHendelse.AKSJONSPUNKT_OPPRETTET);
                }
                if (!AksjonspunktStatus.OPPRETTET.equals(aksjonspunkt.getStatus()) && aksjonspunkt.erAutopunkt() && erBehandlingIFaktaEllerSenereSteg(
                    behandlingId)) {
                    LOG.info(LOGGER_OPPRETTER_PROSESS_TASK, EventHendelse.AKSJONSPUNKT_UTFØRT, aksjonspunkt.getAksjonspunktDefinisjon().getKode());
                    opprettProsessTask(event.getFagsakId(), behandlingId, event.getAktørId(), EventHendelse.AKSJONSPUNKT_UTFØRT);
                }
            }
        }
    }

    public void observerBehandlingAvsluttetEvent(@Observes BehandlingStatusEvent.BehandlingAvsluttetEvent event) {
        opprettProsessTask(event.getFagsakId(), event.getBehandlingId(), event.getAktørId(), EventHendelse.AKSJONSPUNKT_AVBRUTT);
    }

    public void observerAksjonspunktHarEndretBehandlendeEnhetEvent(@Observes BehandlingEnhetEvent event) {
        opprettProsessTask(event.getFagsakId(), event.getBehandlingId(), event.getAktørId(), EventHendelse.AKSJONSPUNKT_HAR_ENDRET_BEHANDLENDE_ENHET);
    }

    public void observerStoppetEvent(@Observes BehandlingskontrollEvent.StoppetEvent event) {
        if (!Fagsystem.FPTILBAKE.equals(fagsystem) && event.getStegStatus().equals(BehandlingStegStatus.INNGANG)) {
            opprettProsessTask(event.getFagsakId(), event.getBehandlingId(), event.getAktørId(), EventHendelse.BEHANDLINGSKONTROLL_EVENT);
        }
    }

    private void opprettProsessTask(long fagsakId, long behandlingId, AktørId aktørId, EventHendelse eventHendelse) {
        if (!Fagsystem.FPTILBAKE.equals(fagsystem)) {
            ProsessTaskData taskData = ProsessTaskData.forProsessTask(K9LosPubliserEventTask.class);
            taskData.setCallIdFraEksisterende();
            taskData.setProperty(K9LosPubliserEventTask.PROPERTY_EVENT_NAME, eventHendelse.name());
            taskData.setBehandling(fagsakId, behandlingId, aktørId.getId());
            taskTjeneste.lagre(taskData);
        }
    }

    private boolean erBehandlingIFaktaEllerSenereSteg(Long behandlingId) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        boolean erIFaktaSteg = FAKTA_FEILUTBETALING.equals(behandling.getAktivtBehandlingSteg());
        boolean erForbiFaktaSteg = behandlingskontrollTjeneste.erStegPassert(behandling, FAKTA_FEILUTBETALING);
        return erIFaktaSteg || erForbiFaktaSteg;
    }

}
