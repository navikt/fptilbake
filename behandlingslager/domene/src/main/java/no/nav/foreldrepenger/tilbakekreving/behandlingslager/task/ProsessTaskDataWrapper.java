package no.nav.foreldrepenger.tilbakekreving.behandlingslager.task;

import no.nav.vedtak.felles.prosesstask.api.ProsessTaskInfo;

public class ProsessTaskDataWrapper {

    private ProsessTaskInfo data;

    private ProsessTaskDataWrapper(ProsessTaskInfo data) {
        this.data = data;
    }

    public static ProsessTaskDataWrapper wrap(ProsessTaskInfo data) {
        return new ProsessTaskDataWrapper(data);
    }

    public Long getBehandlingId() {
        return Long.valueOf(data.getBehandlingId());
    }
}
