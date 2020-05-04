package no.nav.foreldrepenger.tilbakekreving.behandling.steg.iverksettvedtak;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporingRepository;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.SendVedtakHendelserTilDvhTask;
import no.nav.foreldrepenger.tilbakekreving.selvbetjening.klient.task.SendVedtakFattetTilSelvbetjeningTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.util.env.Environment;

@ApplicationScoped
public class ProsessTaskIverksett {

    private static final Logger logger = LoggerFactory.getLogger(ProsessTaskIverksett.class);


    private ProsessTaskRepository taskRepository;
    private BrevSporingRepository brevSporingRepository;

    ProsessTaskIverksett() {
        // for CDI
    }

    @Inject
    public ProsessTaskIverksett(ProsessTaskRepository taskRepository,
                                BrevSporingRepository brevSporingRepository) {
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

        if (brevSporingRepository.harVarselBrevSendtForBehandlingId(behandling.getId())) {
            ProsessTaskData selvbetjeningTask = new ProsessTaskData(SendVedtakFattetTilSelvbetjeningTask.TASKTYPE);
            selvbetjeningTask.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
            taskRepository.lagre(selvbetjeningTask);
        }
        if(erTestMiljø()){
            opprettDvhProsessTask(behandling);
        }
    }

    private void opprettDvhProsessTask(Behandling behandling){
        ProsessTaskData dvhProsessTaskData = new ProsessTaskData(SendVedtakHendelserTilDvhTask.TASKTYPE);
        dvhProsessTaskData.setBehandling(behandling.getFagsakId(),behandling.getId(),behandling.getAktørId().getId());
        taskRepository.lagre(dvhProsessTaskData);
    }

    //midlertidig kode. skal fjernes da dvh er klar
    private boolean erTestMiljø(){
        //foreløpig kun på for testing
        boolean isEnabled = !Environment.current().isProd();
        logger.info("{} er {}", "Send vedtak til DVH", isEnabled ? "skudd på" : "ikke skudd på");
        return isEnabled;
    }
}
