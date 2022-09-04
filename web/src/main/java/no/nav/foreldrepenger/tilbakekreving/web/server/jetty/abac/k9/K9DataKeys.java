package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9;

import static no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9.K9AbacAttributter.RESOURCE_K9_SAK_ANSVARLIG_SAKSBEHANDLER;
import static no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9.K9AbacAttributter.RESOURCE_K9_SAK_BEHANDLINGSSTATUS;
import static no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9.K9AbacAttributter.RESOURCE_K9_SAK_SAKSNUMMER;
import static no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9.K9AbacAttributter.RESOURCE_K9_SAK_SAKSSTATUS;

import no.nav.vedtak.sikkerhet.abac.pdp.RessursDataKey;

public enum K9DataKeys implements RessursDataKey {
    SAKSBEHANDLER(RESOURCE_K9_SAK_ANSVARLIG_SAKSBEHANDLER),
    BEHANDLING_STATUS(RESOURCE_K9_SAK_BEHANDLINGSSTATUS),
    FAGSAK_STATUS(RESOURCE_K9_SAK_SAKSSTATUS),
    SAKSNUMMER(RESOURCE_K9_SAK_SAKSNUMMER);

    private final String key;

    private K9DataKeys(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }
}