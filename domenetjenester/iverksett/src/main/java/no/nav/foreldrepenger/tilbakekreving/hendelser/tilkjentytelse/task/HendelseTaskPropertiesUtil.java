package no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.task;

import java.util.Objects;

import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

/**
 * Holder delte property key navn, og funksjonalitet knyttet rundt task properties.
 */
public class HendelseTaskPropertiesUtil {

    public static final String PROP_IV_SYSTEM = "iverksettingSystem";
    public static final String PROP_SAKSNUMMER = "saksnummer";
    public static final String PROP_FAGSAK_YTELSE_TYPE = "fagsakYtelseType";
    public static final String PROP_VARSELTEKST = "varseltekst";

    private HendelseTaskPropertiesUtil() {}

    private static void validerDelteProperties(ProsessTaskData taskData) {
        Objects.requireNonNull(taskData.getBehandlingId());
        Objects.requireNonNull(taskData.getAktørId());
        Objects.requireNonNull(taskData.getFagsakId());
        Objects.requireNonNull(taskData.getPropertyValue(PROP_IV_SYSTEM), "Mangler property " + PROP_IV_SYSTEM);
    }

    public static void validerTaskDataHåndterHendelse(ProsessTaskData taskData) {
        validerDelteProperties(taskData);
    }

    public static void validerTaskDataOpprettBehandling(ProsessTaskData taskData) {
        validerDelteProperties(taskData);
        Objects.requireNonNull(taskData.getPropertyValue(PROP_SAKSNUMMER), "Mangler saksnummer");
        Objects.requireNonNull(taskData.getPropertyValue(PROP_SAKSNUMMER));
        Objects.requireNonNull(taskData.getPropertyValue(PROP_FAGSAK_YTELSE_TYPE));
    }

}
