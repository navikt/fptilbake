package no.nav.foreldrepenger.tilbakekreving.iverksettevedtak;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.task.AvsluttBehandlingTask;
import no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.task.SendVedtaksbrevTask;
import no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.task.SendØkonomiTibakekerevingsVedtakTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ApplicationScoped
public class ProsessTaskIverksett {

    private ProsessTaskRepository taskRepository;

    ProsessTaskIverksett(){
        // for CDI
    }

    @Inject
    public ProsessTaskIverksett(ProsessTaskRepository taskRepository){
        this.taskRepository = taskRepository;
    }

    public void opprettIverksettingstasker(Behandling behandling) {
        ProsessTaskData avsluttBehandlingTask = new ProsessTaskData(AvsluttBehandlingTask.TASKTYPE);

        ProsessTaskData sendVedtaksbrevTask = new ProsessTaskData(SendVedtaksbrevTask.TASKTYPE);

        ProsessTaskData sendØkonomiTilbakekrevingsvedtakTask = new ProsessTaskData(SendØkonomiTibakekerevingsVedtakTask.TASKTYPE);

        ProsessTaskGruppe taskGruppe = new ProsessTaskGruppe();

        List<ProsessTaskData> parallelle = new ArrayList<>();
        parallelle.add(sendVedtaksbrevTask);
        parallelle.add(sendØkonomiTilbakekrevingsvedtakTask);

        taskGruppe.addNesteParallell(parallelle);
        taskGruppe.addNesteSekvensiell(avsluttBehandlingTask);

        taskGruppe.setBehandling(behandling.getFagsakId(),behandling.getId(),behandling.getAktørId().getId());
        taskGruppe.setCallIdFraEksisterende();

        taskRepository.lagre(taskGruppe);
    }
}
