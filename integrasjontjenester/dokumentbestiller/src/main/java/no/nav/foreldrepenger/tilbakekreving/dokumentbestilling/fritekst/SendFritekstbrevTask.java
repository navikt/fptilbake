package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekst;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@Dependent
@ProsessTask(SendFritekstbrevTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class SendFritekstbrevTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "brev.fritekstbrev";
    private static final Logger logger = LoggerFactory.getLogger(SendFritekstbrevTask.class);

    private FritekstbrevTjeneste fritekstbrevTjeneste;

    @Inject
    public SendFritekstbrevTask(FritekstbrevTjeneste fritekstbrevTjeneste) {
        this.fritekstbrevTjeneste = fritekstbrevTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingId = ProsessTaskDataWrapper.wrap(prosessTaskData).getBehandlingId();
        String fritekst = prosessTaskData.getPayloadAsString();
        String tittel = base64decode(prosessTaskData.getPropertyValue("tittel"));
        String overskrift = base64decode(prosessTaskData.getPropertyValue("overskrift"));
        BrevMottaker brevMottaker = BrevMottaker.valueOf(prosessTaskData.getPropertyValue("mottaker"));

        fritekstbrevTjeneste.sendFritekstbrev(behandlingId, tittel, overskrift, fritekst, brevMottaker);
        logger.info("Sendte fritekstbrev til " + brevMottaker + " for " + behandlingId);
    }

    private static String base64decode(String base64) {
        return new String(Base64.getDecoder().decode(Objects.requireNonNull(base64).getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }
}
