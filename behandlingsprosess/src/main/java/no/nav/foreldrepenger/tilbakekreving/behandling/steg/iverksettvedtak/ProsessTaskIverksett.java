package no.nav.foreldrepenger.tilbakekreving.behandling.steg.iverksettvedtak;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.SendVedtakHendelserTilDvhTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@ApplicationScoped
public class ProsessTaskIverksett {

    private ProsessTaskTjeneste taskTjeneste;

    ProsessTaskIverksett() {
        // for CDI
    }

    @Inject
    public ProsessTaskIverksett(ProsessTaskTjeneste taskTjeneste) {
        this.taskTjeneste = taskTjeneste;
    }

    public void opprettIverksettingstasker(Behandling behandling, boolean sendVedtaksbrev) {
        var taskGruppe = new ProsessTaskGruppe();
        taskGruppe.addNesteSekvensiell(ProsessTaskData.forProsessTask(SendVedtakTilOppdragsystemetTask.class));
        if (sendVedtaksbrev) {
            taskGruppe.addNesteSekvensiell(ProsessTaskData.forProsessTask(SendVedtaksbrevTask.class));
        }
        taskGruppe.addNesteSekvensiell(ProsessTaskData.forProsessTask(AvsluttBehandlingTask.class));
        taskGruppe.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        taskGruppe.setCallIdFraEksisterende();

        opprettDvhProsessTask(behandling, taskGruppe);
        taskTjeneste.lagre(taskGruppe);
    }

    private void opprettDvhProsessTask(Behandling behandling, ProsessTaskGruppe taskGruppe) {
        ProsessTaskData dvhProsessTaskData = ProsessTaskData.forProsessTask(SendVedtakHendelserTilDvhTask.class);
        dvhProsessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        taskGruppe.addNesteSekvensiell(dvhProsessTaskData);
    }

}
