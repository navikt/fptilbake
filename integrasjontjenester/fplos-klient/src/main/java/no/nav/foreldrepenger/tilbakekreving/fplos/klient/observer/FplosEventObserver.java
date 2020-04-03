package no.nav.foreldrepenger.tilbakekreving.fplos.klient.observer;

import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType.FAKTA_FEILUTBETALING;

import java.time.LocalDateTime;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.AksjonspunktTilbakeførtEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.AksjonspunktUtførtEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.AksjonspunkterFunnetEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingEnhetEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingManglerKravgrunnlagFristenEndretEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingManglerKravgrunnlagFristenUtløptEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStatusEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.prosesstask.UtvidetProsessTaskRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.fplos.klient.task.FplosPubliserEventTask;
import no.nav.vedtak.felles.integrasjon.kafka.EventHendelse;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;


@ApplicationScoped
public class FplosEventObserver {

    private static final Logger logger = LoggerFactory.getLogger(FplosEventObserver.class);

    private UtvidetProsessTaskRepository utvidetProsessTaskRepository;
    private ProsessTaskRepository prosessTaskRepository;
    private BehandlingRepository behandlingRepository;

    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    FplosEventObserver() {
        // for CDI proxy
    }

    @Inject
    public FplosEventObserver(BehandlingRepository behandlingRepository,
                              UtvidetProsessTaskRepository utvidetProsessTaskRepository,
                              ProsessTaskRepository prosessTaskRepository,
                              BehandlingskontrollTjeneste behandlingskontrollTjeneste) {
        this.behandlingRepository = behandlingRepository;
        this.utvidetProsessTaskRepository = utvidetProsessTaskRepository;
        this.prosessTaskRepository = prosessTaskRepository;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
    }

    public void observerAksjonpunktFunnetEvent(@Observes AksjonspunkterFunnetEvent event) {
        Long behandlingId = event.getBehandlingId();
        for (Aksjonspunkt aksjonspunkt : event.getAksjonspunkter()) {
            if (aksjonspunkt.erManuell() || erBehandlingIFaktaEllerSenereSteg(behandlingId)) {
                logger.info("Oppretter prosess task for å publisere event={} til fplos for aksjonspunkt={}", EventHendelse.AKSJONSPUNKT_OPPRETTET, aksjonspunkt.getAksjonspunktDefinisjon().getKode());
                opprettProsessTask(event.getFagsakId(), behandlingId, event.getAktørId(), EventHendelse.AKSJONSPUNKT_OPPRETTET);
            }
        }
    }

    public void observerAksjonpunktUtførtEvent(@Observes AksjonspunktUtførtEvent event) {
        Long behandlingId = event.getBehandlingId();
        for (Aksjonspunkt aksjonspunkt : event.getAksjonspunkter()) {
            if (aksjonspunkt.erAutopunkt() && erBehandlingIFaktaEllerSenereSteg(behandlingId)) {
                logger.info("Oppretter prosess task for å publisere event={} til fplos for aksjonspunkt={}", EventHendelse.AKSJONSPUNKT_UTFØRT, aksjonspunkt.getAksjonspunktDefinisjon().getKode());
                opprettProsessTask(event.getFagsakId(), behandlingId, event.getAktørId(), EventHendelse.AKSJONSPUNKT_UTFØRT);
            }
        }
    }

    public void observerAksjonpunktTilbakeførtEvent(@Observes AksjonspunktTilbakeførtEvent event) {
        logger.info("Oppretter prosess task for å publisere event={} til fplos for aksjonspunkt={}", EventHendelse.AKSJONSPUNKT_TILBAKEFØR, event.getAksjonspunkter().get(0).getAksjonspunktDefinisjon().getKode());
        opprettProsessTask(event.getFagsakId(), event.getBehandlingId(), event.getAktørId(), EventHendelse.AKSJONSPUNKT_TILBAKEFØR);
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

    public void observerBehandlingFristenEndretEvent(@Observes BehandlingManglerKravgrunnlagFristenUtløptEvent utløptEvent) {
        logger.info("Oppretter prosess task for å publisere event={} til fplos for aksjonspunkt={}", EventHendelse.AKSJONSPUNKT_OPPRETTET,
            AksjonspunktDefinisjon.VURDER_HENLEGGELSE_MANGLER_KRAVGRUNNLAG.getKode());
        opprettProsessTask(utløptEvent.getFagsakId(), utløptEvent.getBehandlingId(), utløptEvent.getAktørId(),
            EventHendelse.AKSJONSPUNKT_OPPRETTET, AksjonspunktStatus.OPPRETTET, utløptEvent.getFristDato());
    }

    public void observerBehandlingFristenEndretEvent(@Observes BehandlingManglerKravgrunnlagFristenEndretEvent fristenEndretEvent) {
        logger.info("Oppretter prosess task for å publisere event={} til fplos for aksjonspunkt={}", EventHendelse.AKSJONSPUNKT_AVBRUTT,
            AksjonspunktDefinisjon.VURDER_HENLEGGELSE_MANGLER_KRAVGRUNNLAG.getKode());
        opprettProsessTask(fristenEndretEvent.getFagsakId(), fristenEndretEvent.getBehandlingId(), fristenEndretEvent.getAktørId(),
            EventHendelse.AKSJONSPUNKT_AVBRUTT, AksjonspunktStatus.AVBRUTT, fristenEndretEvent.getFristDato());
    }

    private void opprettProsessTask(long fagsakId, long behandlingId, AktørId aktørId, EventHendelse eventHendelse) {
        ProsessTaskData taskData = lagFellesProsessTaskData(fagsakId, behandlingId, aktørId, eventHendelse);
        prosessTaskRepository.lagre(taskData);
    }

    private void opprettProsessTask(long fagsakId, long behandlingId, AktørId aktørId, EventHendelse eventHendelse,
                                    AksjonspunktStatus aksjonspunktStatus, LocalDateTime fristTid) {
        ProsessTaskData taskData = lagFellesProsessTaskData(fagsakId, behandlingId, aktørId, eventHendelse);

        taskData.setProperty(FplosPubliserEventTask.PROPERTY_KRAVGRUNNLAG_MANGLER_AKSJONSPUNKT_STATUS_KODE, aksjonspunktStatus.getKode());
        taskData.setProperty(FplosPubliserEventTask.PROPERTY_KRAVGRUNNLAG_MANGLER_FRIST_TID, fristTid.toString());

        prosessTaskRepository.lagre(taskData);
    }

    private ProsessTaskData lagFellesProsessTaskData(long fagsakId, long behandlingId, AktørId aktørId, EventHendelse eventHendelse) {
        String gruppe = "los" + behandlingId;
        Optional<ProsessTaskData> eksisterendeProsessTask = utvidetProsessTaskRepository.finnSisteProsessTaskForProsessTaskGruppe(FplosPubliserEventTask.TASKTYPE, gruppe);
        long sekvens = eksisterendeProsessTask.isPresent() ? Long.valueOf(eksisterendeProsessTask.get().getSekvens()) + 1 : 1l;

        ProsessTaskData taskData = new ProsessTaskData(FplosPubliserEventTask.TASKTYPE);
        taskData.setCallIdFraEksisterende();
        taskData.setPrioritet(50);
        taskData.setProperty(FplosPubliserEventTask.PROPERTY_EVENT_NAME, eventHendelse.name());
        taskData.setBehandling(fagsakId, behandlingId, aktørId.getId());
        taskData.setGruppe(gruppe);
        taskData.setSekvens(String.format("%04d", sekvens));
        return taskData;
    }

    private boolean erBehandlingIFaktaEllerSenereSteg(Long behandlingId) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        boolean erIFaktaSteg = FAKTA_FEILUTBETALING.equals(behandling.getAktivtBehandlingSteg());
        boolean erForbiFaktaSteg = behandlingskontrollTjeneste.erStegPassert(behandling, FAKTA_FEILUTBETALING);
        return erIFaktaSteg || erForbiFaktaSteg;
    }
}
