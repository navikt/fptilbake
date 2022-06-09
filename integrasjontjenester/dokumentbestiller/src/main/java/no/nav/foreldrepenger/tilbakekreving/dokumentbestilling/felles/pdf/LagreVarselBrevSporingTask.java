package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask("brev.sporing.varsel")
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class LagreVarselBrevSporingTask implements ProsessTaskHandler {

    protected static final String VARSLET_BELOEP = "varsletBeloep";
    private VarselRepository varselRepository;

    public LagreVarselBrevSporingTask() {
        //CDI proxy
    }

    @Inject
    public LagreVarselBrevSporingTask(VarselRepository varselRepository) {
        this.varselRepository = varselRepository;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingId = ProsessTaskDataWrapper.wrap(prosessTaskData).getBehandlingId();
        String varseltekst = prosessTaskData.getPayloadAsString();
        Long varseltBeløp = Long.valueOf(prosessTaskData.getPropertyValue(VARSLET_BELOEP));

        varselRepository.lagre(behandlingId, varseltekst, varseltBeløp);
    }
}
