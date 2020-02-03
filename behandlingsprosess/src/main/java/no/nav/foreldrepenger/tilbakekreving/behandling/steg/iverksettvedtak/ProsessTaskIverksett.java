package no.nav.foreldrepenger.tilbakekreving.behandling.steg.iverksettvedtak;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ApplicationScoped
public class ProsessTaskIverksett {

    private ProsessTaskRepository taskRepository;

    ProsessTaskIverksett() {
        // for CDI
    }

    @Inject
    public ProsessTaskIverksett(ProsessTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
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
    }
}
