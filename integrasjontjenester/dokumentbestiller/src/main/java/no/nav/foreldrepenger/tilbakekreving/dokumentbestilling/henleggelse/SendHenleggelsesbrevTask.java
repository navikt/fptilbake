package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.henleggelse;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.VergeRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

import java.util.Optional;
import java.util.UUID;

@Dependent
@ProsessTask("brev.sendhenleggelse")
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class SendHenleggelsesbrevTask implements ProsessTaskHandler {

    public static final String BESTILLING_UUID = "bestillingUuid";

    private HenleggelsesbrevTjeneste henleggelsesbrevTjeneste;
    private VergeRepository vergeRepository;

    @Inject
    public SendHenleggelsesbrevTask(HenleggelsesbrevTjeneste henleggelsesbrevTjeneste,
                                    VergeRepository vergeRepository) {
        this.henleggelsesbrevTjeneste = henleggelsesbrevTjeneste;
        this.vergeRepository = vergeRepository;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingId = ProsessTaskDataWrapper.wrap(prosessTaskData).getBehandlingId();
        String fritekst = prosessTaskData.getPayloadAsString();
        var unikBestillingUuid = UUID.fromString(Optional.of(prosessTaskData.getPropertyValue(BESTILLING_UUID)).orElseThrow());

        if (vergeRepository.finnesVerge(behandlingId)) {
            henleggelsesbrevTjeneste.sendHenleggelsebrev(behandlingId, fritekst, BrevMottaker.VERGE, unikBestillingUuid);
        }
        henleggelsesbrevTjeneste.sendHenleggelsebrev(behandlingId, fritekst, BrevMottaker.BRUKER, unikBestillingUuid);
    }
}
