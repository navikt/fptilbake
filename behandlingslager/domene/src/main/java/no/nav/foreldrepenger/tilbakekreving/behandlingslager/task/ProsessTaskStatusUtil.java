package no.nav.foreldrepenger.tilbakekreving.behandlingslager.task;

import java.util.Set;

import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;

public class ProsessTaskStatusUtil {

    public static final Set<ProsessTaskStatus> FERDIG_STATUSER = Set.of(ProsessTaskStatus.FERDIG, ProsessTaskStatus.KJOERT);
}
