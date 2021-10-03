package no.nav.foreldrepenger.tilbakekreving.behandling.steg.iverksettvedtak;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporingRepository;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.SendVedtakHendelserTilDvhTask;
import no.nav.foreldrepenger.tilbakekreving.selvbetjening.SelvbetjeningTilbakekrevingStøtte;
import no.nav.foreldrepenger.tilbakekreving.selvbetjening.klient.task.SendVedtakFattetTilSelvbetjeningTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@ApplicationScoped
public class ProsessTaskIverksett {

    private ProsessTaskTjeneste taskTjeneste;
    private BrevSporingRepository brevSporingRepository;

    ProsessTaskIverksett() {
        // for CDI
    }

    @Inject
    public ProsessTaskIverksett(ProsessTaskTjeneste taskTjeneste, BrevSporingRepository brevSporingRepository) {
        this.taskTjeneste = taskTjeneste;
        this.brevSporingRepository = brevSporingRepository;
    }

    public void opprettIverksettingstasker(Behandling behandling, boolean sendVedtaksbrev) {
        ProsessTaskGruppe taskGruppe = new ProsessTaskGruppe();
        taskGruppe.addNesteSekvensiell(ProsessTaskData.forProsessTask(SendØkonomiTibakekerevingsVedtakTask.class));
        if (sendVedtaksbrev) {
            taskGruppe.addNesteSekvensiell(ProsessTaskData.forProsessTask(SendVedtaksbrevTask.class));
        }
        taskGruppe.addNesteSekvensiell(ProsessTaskData.forProsessTask(AvsluttBehandlingTask.class));
        taskGruppe.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        taskGruppe.setCallIdFraEksisterende();

        if (SelvbetjeningTilbakekrevingStøtte.harStøtteFor(behandling) && brevSporingRepository.harVarselBrevSendtForBehandlingId(behandling.getId())) {
            ProsessTaskData selvbetjeningTask = ProsessTaskData.forProsessTask(SendVedtakFattetTilSelvbetjeningTask.class);
            selvbetjeningTask.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
            taskGruppe.addNesteSekvensiell(selvbetjeningTask);
        }
        opprettDvhProsessTask(behandling, taskGruppe);
        taskTjeneste.lagre(taskGruppe);
    }

    private void opprettDvhProsessTask(Behandling behandling, ProsessTaskGruppe taskGruppe) {
        ProsessTaskData dvhProsessTaskData = ProsessTaskData.forProsessTask(SendVedtakHendelserTilDvhTask.class);
        dvhProsessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        taskGruppe.addNesteSekvensiell(dvhProsessTaskData);
    }

}
