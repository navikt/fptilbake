package no.nav.foreldrepenger.tilbakekreving.fplos.klient.observer;

import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType.FAKTA_FEILUTBETALING;

import java.time.LocalDateTime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.AksjonspunktStatusEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.BehandlingEnhetEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.BehandlingManglerKravgrunnlagFristenEndretEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.BehandlingManglerKravgrunnlagFristenUtløptEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.BehandlingStatusEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.BehandlingskontrollEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktKodeDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.fplos.klient.task.FplosPubliserEventTask;
import no.nav.vedtak.felles.integrasjon.kafka.EventHendelse;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;


@ApplicationScoped
public class FplosEventObserver {

    private static final Logger logger = LoggerFactory.getLogger(FplosEventObserver.class);
    private static final String LOGGER_OPPRETTER_PROSESS_TASK = "Oppretter prosess task for å publisere event={} til fplos for aksjonspunkt={}";

    private ProsessTaskTjeneste taskTjeneste;
    private BehandlingRepository behandlingRepository;

    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    FplosEventObserver() {
        // for CDI proxy
    }

    @Inject
    public FplosEventObserver(BehandlingRepository behandlingRepository,
                              ProsessTaskTjeneste taskTjeneste,
                              BehandlingskontrollTjeneste behandlingskontrollTjeneste) {
        this.behandlingRepository = behandlingRepository;
        this.taskTjeneste = taskTjeneste;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
    }

    public void observerAksjonpunktStatusEvent(@Observes AksjonspunktStatusEvent event) {
        Long behandlingId = event.getBehandlingId();
        for (Aksjonspunkt aksjonspunkt : event.getAksjonspunkter()) {
            if (AksjonspunktStatus.OPPRETTET.equals(aksjonspunkt.getStatus()) && (aksjonspunkt.erManuell() || erBehandlingIFaktaEllerSenereSteg(behandlingId))) {
                logger.info(LOGGER_OPPRETTER_PROSESS_TASK, EventHendelse.AKSJONSPUNKT_OPPRETTET, aksjonspunkt.getAksjonspunktDefinisjon().getKode());
                opprettProsessTask(event.getFagsakId(), behandlingId, event.getAktørId(), EventHendelse.AKSJONSPUNKT_OPPRETTET);
            }
            if (!AksjonspunktStatus.OPPRETTET.equals(aksjonspunkt.getStatus()) && aksjonspunkt.erAutopunkt() && erBehandlingIFaktaEllerSenereSteg(behandlingId)) {
                logger.info(LOGGER_OPPRETTER_PROSESS_TASK, EventHendelse.AKSJONSPUNKT_UTFØRT, aksjonspunkt.getAksjonspunktDefinisjon().getKode());
                opprettProsessTask(event.getFagsakId(), behandlingId, event.getAktørId(), EventHendelse.AKSJONSPUNKT_UTFØRT);
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
        if (event.getStegStatus().equals(BehandlingStegStatus.INNGANG)) {
            opprettProsessTask(event.getFagsakId(), event.getBehandlingId(), event.getAktørId(), EventHendelse.BEHANDLINGSKONTROLL_EVENT);
        }
    }

    public void observerBehandlingFristenUtløptEvent(@Observes BehandlingManglerKravgrunnlagFristenUtløptEvent utløptEvent) {
        logger.info(LOGGER_OPPRETTER_PROSESS_TASK, EventHendelse.AKSJONSPUNKT_OPPRETTET,
            AksjonspunktKodeDefinisjon.VURDER_HENLEGGELSE_MANGLER_KRAVGRUNNLAG);
        opprettProsessTask(utløptEvent.getFagsakId(), utløptEvent.getBehandlingId(), utløptEvent.getAktørId(),
            EventHendelse.AKSJONSPUNKT_OPPRETTET, AksjonspunktStatus.OPPRETTET, utløptEvent.getFristDato());
    }

    public void observerBehandlingFristenEndretEvent(@Observes BehandlingManglerKravgrunnlagFristenEndretEvent fristenEndretEvent) {
        logger.info(LOGGER_OPPRETTER_PROSESS_TASK, EventHendelse.AKSJONSPUNKT_AVBRUTT,
            AksjonspunktKodeDefinisjon.VURDER_HENLEGGELSE_MANGLER_KRAVGRUNNLAG);
        opprettProsessTask(fristenEndretEvent.getFagsakId(), fristenEndretEvent.getBehandlingId(), fristenEndretEvent.getAktørId(),
            EventHendelse.AKSJONSPUNKT_AVBRUTT, AksjonspunktStatus.AVBRUTT, fristenEndretEvent.getFristDato());
    }

    private void opprettProsessTask(long fagsakId, long behandlingId, AktørId aktørId, EventHendelse eventHendelse) {
        ProsessTaskData taskData = lagFellesProsessTaskData(fagsakId, behandlingId, aktørId, eventHendelse);
        taskTjeneste.lagre(taskData);
    }

    private void opprettProsessTask(long fagsakId, long behandlingId, AktørId aktørId, EventHendelse eventHendelse,
                                    AksjonspunktStatus aksjonspunktStatus, LocalDateTime fristTid) {
        ProsessTaskData taskData = lagFellesProsessTaskData(fagsakId, behandlingId, aktørId, eventHendelse);

        taskData.setProperty(FplosPubliserEventTask.PROPERTY_KRAVGRUNNLAG_MANGLER_AKSJONSPUNKT_STATUS_KODE, aksjonspunktStatus.getKode());
        taskData.setProperty(FplosPubliserEventTask.PROPERTY_KRAVGRUNNLAG_MANGLER_FRIST_TID, fristTid.toString());

        taskTjeneste.lagre(taskData);
    }

    private ProsessTaskData lagFellesProsessTaskData(long fagsakId, long behandlingId, AktørId aktørId, EventHendelse eventHendelse) {
        ProsessTaskData taskData = ProsessTaskData.forProsessTask(FplosPubliserEventTask.class);
        taskData.setCallIdFraEksisterende();
        taskData.setPrioritet(50);
        taskData.setProperty(FplosPubliserEventTask.PROPERTY_EVENT_NAME, eventHendelse.name());
        taskData.setBehandling(fagsakId, behandlingId, aktørId.getId());
        return taskData;
    }

    private boolean erBehandlingIFaktaEllerSenereSteg(Long behandlingId) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        boolean erIFaktaSteg = FAKTA_FEILUTBETALING.equals(behandling.getAktivtBehandlingSteg());
        boolean erForbiFaktaSteg = behandlingskontrollTjeneste.erStegPassert(behandling, FAKTA_FEILUTBETALING);
        return erIFaktaSteg || erForbiFaktaSteg;
    }

}
