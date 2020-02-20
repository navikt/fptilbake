package no.nav.foreldrepenger.tilbakekreving.fplos.klient.observer;

import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType.FAKTA_FEILUTBETALING;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.AksjonspunktTilbakeførtEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.AksjonspunktUtførtEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.AksjonspunkterFunnetEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingEnhetEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStatusEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.fplos.klient.task.FplosPubliserEventTask;
import no.nav.vedtak.felles.integrasjon.kafka.EventHendelse;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;


@ApplicationScoped
public class FplosEventObserver {

    private ProsessTaskRepository prosessTaskRepository;
    private BehandlingRepository behandlingRepository;

    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    FplosEventObserver() {
        // for CDI proxy
    }

    @Inject
    public FplosEventObserver(BehandlingRepository behandlingRepository,
                              ProsessTaskRepository prosessTaskRepository,
                              BehandlingskontrollTjeneste behandlingskontrollTjeneste) {
        this.behandlingRepository = behandlingRepository;
        this.prosessTaskRepository = prosessTaskRepository;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
    }

    public void observerAksjonpunktFunnetEvent(@Observes AksjonspunkterFunnetEvent event) {
        Long behandlingId = event.getBehandlingId();
        for (Aksjonspunkt aksjonspunkt : event.getAksjonspunkter()) {
            if (aksjonspunkt.erManuell() || erBehandlingIFaktaEllerSenereSteg(behandlingId)) {
                opprettProsessTask(event.getFagsakId(),behandlingId,event.getAktørId(), EventHendelse.AKSJONSPUNKT_OPPRETTET);
            }
        }
    }

    public void observerAksjonpunktUtførtEvent(@Observes AksjonspunktUtførtEvent event) {
        Long behandlingId = event.getBehandlingId();
        for (Aksjonspunkt aksjonspunkt : event.getAksjonspunkter()) {
            if (aksjonspunkt.erAutopunkt() && erBehandlingIFaktaEllerSenereSteg(behandlingId)) {
                opprettProsessTask(event.getFagsakId(),behandlingId,event.getAktørId(), EventHendelse.AKSJONSPUNKT_UTFØRT);
            }
        }
    }

    public void observerAksjonpunktTilbakeførtEvent(@Observes AksjonspunktTilbakeførtEvent event) {
        opprettProsessTask(event.getFagsakId(),event.getBehandlingId(),event.getAktørId(), EventHendelse.AKSJONSPUNKT_TILBAKEFØR);
    }

    public void observerBehandlingAvsluttetEvent(@Observes BehandlingStatusEvent.BehandlingAvsluttetEvent event) {
        opprettProsessTask(event.getFagsakId(),event.getBehandlingId(),event.getAktørId(), EventHendelse.AKSJONSPUNKT_AVBRUTT);
    }

    public void observerAksjonspunktHarEndretBehandlendeEnhetEvent(@Observes BehandlingEnhetEvent event) {
        opprettProsessTask(event.getFagsakId(),event.getBehandlingId(),event.getAktørId(), EventHendelse.AKSJONSPUNKT_HAR_ENDRET_BEHANDLENDE_ENHET);
    }


    private void opprettProsessTask(long fagsakId, long behandlingId, AktørId aktørId, EventHendelse eventHendelse) {
        ProsessTaskData taskData = new ProsessTaskData(FplosPubliserEventTask.TASKTYPE);
        taskData.setCallIdFraEksisterende();
        taskData.setPrioritet(50);
        taskData.setProperty(FplosPubliserEventTask.PROPERTY_EVENT_NAME, eventHendelse.name());
        taskData.setBehandling(fagsakId, behandlingId, aktørId.getId());

        prosessTaskRepository.lagre(taskData);
    }

    private boolean erBehandlingIFaktaEllerSenereSteg(Long behandlingId) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        boolean erIFaktaSteg = FAKTA_FEILUTBETALING.equals(behandling.getAktivtBehandlingSteg());
        boolean erForbiFaktaSteg = behandlingskontrollTjeneste.erStegPassert(behandling, FAKTA_FEILUTBETALING);
        return erIFaktaSteg || erForbiFaktaSteg;
    }
}
