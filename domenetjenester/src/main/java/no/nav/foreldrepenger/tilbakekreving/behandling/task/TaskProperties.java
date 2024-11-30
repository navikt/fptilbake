package no.nav.foreldrepenger.tilbakekreving.behandling.task;

public class TaskProperties {

    public static final String EKSTERN_BEHANDLING_UUID = "eksternBehandlingUuid";
    public static final String EKSTERN_BEHANDLING_ID = "ekstenBehandlingId";
    public static final String HENVISNING = "henvisning";
    public static final String FAGSAK_YTELSE_TYPE = "fagYtelseType";
    public static final String BEHANDLING_TYPE = "behandlingType";

    // Kravgrunnlag + status
    public static final String PROPERTY_MOTTATT_XML_ID = "mottattXmlId";
    public static final String PROPERTY_ORIGINAL_BEHANDLING_ID = "origBehandlingId";

    public static final String ROOT_ELEMENT_KRAV_VEDTAK_STATUS_XML = "endringKravOgVedtakstatus";
    public static final String ROOT_ELEMENT_KRAVGRUNNLAG_XML = "detaljertKravgrunnlagMelding";

    private TaskProperties() {

    }
}
