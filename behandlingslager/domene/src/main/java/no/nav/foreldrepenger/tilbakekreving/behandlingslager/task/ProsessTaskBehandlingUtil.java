package no.nav.foreldrepenger.tilbakekreving.behandlingslager.task;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

public class ProsessTaskBehandlingUtil {
    public static void setBehandling(ProsessTaskData data, Behandling behandling){
        String saksnummer = behandling.getFagsak().getSaksnummer().getVerdi();
        String behandlingId = behandling.getId().toString();
        String aktørId = behandling.getAktørId().getId();
        data.setBehandling(saksnummer, behandlingId, aktørId);
    }
}
