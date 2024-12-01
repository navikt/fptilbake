package no.nav.foreldrepenger.tilbakekreving.behandling.steg.iverksettvedtak;

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.task.TaskProperties;
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
            opprettVedtaksbrevProsessTask(taskGruppe);
        }
        taskGruppe.addNesteSekvensiell(ProsessTaskData.forProsessTask(AvsluttBehandlingTask.class));
        taskGruppe.addNesteSekvensiell(ProsessTaskData.forProsessTask(SendVedtakHendelserTilDvhTask.class));

        taskGruppe.setBehandling(behandling.getSaksnummer().getVerdi(), behandling.getFagsakId(), behandling.getId());
        taskGruppe.setCallIdFraEksisterende();

        taskTjeneste.lagre(taskGruppe);
    }

    private static void opprettVedtaksbrevProsessTask(ProsessTaskGruppe taskGruppe) {
        var taskData = ProsessTaskData.forProsessTask(SendVedtaksbrevTask.class);
        taskData.setProperty(TaskProperties.BESTILLING_UUID,
            UUID.randomUUID().toString()); // Brukes som eksternReferanseId ved journalføring av brev
        taskGruppe.addNesteSekvensiell(taskData);
    }
}
