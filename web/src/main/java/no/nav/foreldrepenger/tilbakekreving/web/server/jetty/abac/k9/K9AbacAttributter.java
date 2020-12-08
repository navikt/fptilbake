package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9;

import no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter;

public class K9AbacAttributter {
    public static final String XACML_1_0_ACTION_ACTION_ID = NavAbacCommonAttributter.XACML10_ACTION_ACTION_ID;

    public static final String RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE = NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE;
    public static final String RESOURCE_FELLES_PERSON_FNR = NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_FNR;
    public static final String RESOURCE_FELLES_DOMENE = NavAbacCommonAttributter.RESOURCE_FELLES_DOMENE;
    public static final String RESOURCE_FELLES_RESOURCE_TYPE = NavAbacCommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE;

    public static final String RESOURCE_K9_SAK_SAKSSTATUS = "no.nav.abac.attributter.resource.k9.sak.saksstatus";
    public static final String RESOURCE_K9_SAK_BEHANDLINGSSTATUS = "no.nav.abac.attributter.resource.k9.sak.behandlingsstatus";
    public static final String RESOURCE_K9_SAK_AKSJONSPUNKT_TYPE = "no.nav.abac.attributter.resource.k9.sak.aksjonspunkt_type";
    public static final String RESOURCE_K9_SAK_SAKSNUMMER = "no.nav.abac.attributter.resource.k9.saksnr";
    public static final String RESOURCE_K9_SAK_ANSVARLIG_SAKSBEHANDLER = "no.nav.abac.attributter.resource.k9.sak.ansvarlig_saksbehandler";

}
