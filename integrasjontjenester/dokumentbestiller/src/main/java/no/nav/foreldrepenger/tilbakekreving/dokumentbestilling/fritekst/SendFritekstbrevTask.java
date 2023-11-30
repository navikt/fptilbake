package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekst;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

/**
 * @deprecated antageligvis ikke i bruk og kan fjernes
 */
@Deprecated(forRemoval = true)
@Dependent
@ProsessTask("brev.fritekstbrev")
public class SendFritekstbrevTask implements ProsessTaskHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SendFritekstbrevTask.class);
    public static final String BESTILLING_UUID = "bestillingUuid";

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
        var unikBestillingUuid = UUID.fromString(Optional.ofNullable(prosessTaskData.getPropertyValue(BESTILLING_UUID)).orElse(UUID.randomUUID().toString()));


        fritekstbrevTjeneste.sendFritekstbrev(behandlingId, tittel, overskrift, fritekst, brevMottaker, unikBestillingUuid);
        LOG.info("Sendte fritekstbrev til {} for {}", brevMottaker, behandlingId);
    }

    private static String base64decode(String base64) {
        return new String(Base64.getDecoder().decode(Objects.requireNonNull(base64).getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }
}
