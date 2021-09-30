package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.hendelse;

import no.nav.vedtak.felles.prosesstask.api.TaskType;

public class TaskProperties {

    public static final String EKSTERN_BEHANDLING_UUID = "eksternBehandlingUuid";
    public static final String EKSTERN_BEHANDLING_ID = "ekstenBehandlingId";
    public static final String HENVISNING = "henvisning";
    public static final String SAKSNUMMER = "saksnummer";
    public static final String FAGSAK_YTELSE_TYPE = "fagYtelseType";
    public static final String BEHANDLING_TYPE = "behandlingType";
    public static final TaskType OPPRETT_BEHANDLING_TASK_TYPE = new TaskType("hendelser.opprettBehandling"); // TODO refaktorer og kolokalisering med task så opprettes fra klasse

    private TaskProperties(){

    }
}
