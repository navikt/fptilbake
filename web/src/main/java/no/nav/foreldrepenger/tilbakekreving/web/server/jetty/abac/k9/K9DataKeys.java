package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9;

import no.nav.vedtak.sikkerhet.abac.pdp.RessursDataKey;

public enum K9DataKeys implements RessursDataKey {
    SAKSBEHANDLER("no.nav.abac.attributter.resource.k9.sak.ansvarlig_saksbehandler"),
    BEHANDLING_STATUS("no.nav.abac.attributter.resource.k9.sak.behandlingsstatus"),
    FAGSAK_STATUS("no.nav.abac.attributter.resource.k9.sak.saksstatus"),
    SAKSNUMMER("no.nav.abac.attributter.resource.k9.saksnr");

    private final String key;

    private K9DataKeys(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }
}