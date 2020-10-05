package no.nav.foreldrepenger.tilbakekreving.behandling.steg.iverksettvedtak;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporingRepository;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.SendVedtakHendelserTilDvhTask;
import no.nav.foreldrepenger.tilbakekreving.selvbetjening.klient.task.SendBeskjedUtsendtVarselTilSelvbetjeningTask;
import no.nav.foreldrepenger.tilbakekreving.selvbetjening.klient.task.SendVedtakFattetTilSelvbetjeningTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ApplicationScoped
public class ProsessTaskIverksett {

    private ProsessTaskRepository taskRepository;
    private BrevSporingRepository brevSporingRepository;

    ProsessTaskIverksett() {
        // for CDI
    }

    @Inject
    public ProsessTaskIverksett(ProsessTaskRepository taskRepository, BrevSporingRepository brevSporingRepository) {
        this.taskRepository = taskRepository;
        this.brevSporingRepository = brevSporingRepository;
    }

    public void opprettIverksettingstasker(Behandling behandling, boolean sendVedtaksbrev) {
        ProsessTaskGruppe taskGruppe = new ProsessTaskGruppe();
        taskGruppe.addNesteSekvensiell(new ProsessTaskData(SendØkonomiTibakekerevingsVedtakTask.TASKTYPE));
        if (sendVedtaksbrev) {
            taskGruppe.addNesteSekvensiell(new ProsessTaskData(SendVedtaksbrevTask.TASKTYPE));
        }
        taskGruppe.addNesteSekvensiell(new ProsessTaskData(AvsluttBehandlingTask.TASKTYPE));
        taskGruppe.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        taskGruppe.setCallIdFraEksisterende();

        taskRepository.lagre(taskGruppe);

        if (SendBeskjedUtsendtVarselTilSelvbetjeningTask.kanSendeVarsel(behandling) && brevSporingRepository.harVarselBrevSendtForBehandlingId(behandling.getId())) {
            ProsessTaskData selvbetjeningTask = new ProsessTaskData(SendVedtakFattetTilSelvbetjeningTask.TASKTYPE);
            selvbetjeningTask.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
            taskRepository.lagre(selvbetjeningTask);
        }
        opprettDvhProsessTask(behandling);
    }

    private void opprettDvhProsessTask(Behandling behandling) {
        ProsessTaskData dvhProsessTaskData = new ProsessTaskData(SendVedtakHendelserTilDvhTask.TASKTYPE);
        dvhProsessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        taskRepository.lagre(dvhProsessTaskData);
    }

}
