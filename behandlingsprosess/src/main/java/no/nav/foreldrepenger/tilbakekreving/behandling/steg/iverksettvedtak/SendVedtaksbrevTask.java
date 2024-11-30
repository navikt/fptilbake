package no.nav.foreldrepenger.tilbakekreving.behandling.steg.iverksettvedtak;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.task.TaskProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.VergeRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.VedtaksbrevTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.log.mdc.MdcExtendedLogContext;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
@ProsessTask("iverksetteVedtak.sendVedtaksbrev")
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class SendVedtaksbrevTask implements ProsessTaskHandler {

    private static final Logger log = LoggerFactory.getLogger(SendVedtaksbrevTask.class);
    private static final MdcExtendedLogContext LOG_CONTEXT = MdcExtendedLogContext.getContext("prosess");

    private VergeRepository vergeRepository;
    private VedtaksbrevTjeneste vedtaksbrevTjeneste;

    SendVedtaksbrevTask() {
        // for CDI proxy
    }

    @Inject
    public SendVedtaksbrevTask(VergeRepository vergeRepository,
                               VedtaksbrevTjeneste vedtaksbrevTjeneste) {
        this.vergeRepository = vergeRepository;
        this.vedtaksbrevTjeneste = vedtaksbrevTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingId = ProsessTaskDataWrapper.wrap(prosessTaskData).getBehandlingId();
        var unikBestillingUuid = UUID.fromString(Optional.of(prosessTaskData.getPropertyValue(TaskProperties.BESTILLING_UUID)).orElseThrow());
        LOG_CONTEXT.add("behandling", behandlingId);
        if (vergeRepository.finnesVerge(behandlingId)) {
            vedtaksbrevTjeneste.sendVedtaksbrev(behandlingId, BrevMottaker.VERGE, unikBestillingUuid);
        }
        vedtaksbrevTjeneste.sendVedtaksbrev(behandlingId, BrevMottaker.BRUKER, unikBestillingUuid);
        log.info("Utført for behandling: {}", behandlingId);
    }
}
