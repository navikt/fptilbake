package no.nav.foreldrepenger.tilbakekreving.behandling.steg.iverksettvedtak;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporingRepository;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.SendVedtakHendelserTilDvhTask;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.VedtakOppsummeringTjeneste;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping.VedtakOppsummeringMapper;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.vedtak.VedtakOppsummering;
import no.nav.foreldrepenger.tilbakekreving.selvbetjening.klient.task.SendVedtakFattetTilSelvbetjeningTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ApplicationScoped
public class ProsessTaskIverksett {

    private ProsessTaskRepository taskRepository;
    private BrevSporingRepository brevSporingRepository;
    private VedtakOppsummeringTjeneste vedtakOppsummeringTjeneste;

    ProsessTaskIverksett() {
        // for CDI
    }

    @Inject
    public ProsessTaskIverksett(ProsessTaskRepository taskRepository,
                                BrevSporingRepository brevSporingRepository,
                                VedtakOppsummeringTjeneste vedtakOppsummeringTjeneste) {
        this.taskRepository = taskRepository;
        this.brevSporingRepository = brevSporingRepository;
        this.vedtakOppsummeringTjeneste = vedtakOppsummeringTjeneste;
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

        if (brevSporingRepository.harVarselBrevSendtForBehandlingId(behandling.getId())) {
            ProsessTaskData selvbetjeningTask = new ProsessTaskData(SendVedtakFattetTilSelvbetjeningTask.TASKTYPE);
            selvbetjeningTask.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
            taskRepository.lagre(selvbetjeningTask);
        }
        opprettDvhProsessTask(behandling.getId());
    }

    private void opprettDvhProsessTask(long behandlingId){
        ProsessTaskData dvhProsessTaskData = new ProsessTaskData(SendVedtakHendelserTilDvhTask.TASKTYPE);
        VedtakOppsummering vedtakOppsummering = vedtakOppsummeringTjeneste.hentVedtakOppsummering(behandlingId);
        dvhProsessTaskData.setPayload(VedtakOppsummeringMapper.tilJsonString(vedtakOppsummering));
        taskRepository.lagre(dvhProsessTaskData);
    }
}
