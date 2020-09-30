package no.nav.foreldrepenger.tilbakekreving.behandlingslager.task;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

public class ProsessTaskBehandlingUtil {
    public static void setBehandling(ProsessTaskData data, Behandling behandling) {
        Long fagsakId = behandling.getFagsak().getId();
        Long behandlingId = behandling.getId();
        String aktørId = behandling.getAktørId().getId();
        data.setBehandling(fagsakId, behandlingId, aktørId);
    }
}
