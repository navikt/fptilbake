package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9;

public enum K9DataKeys{
    SAKSBEHANDLER("no.nav.abac.attributter.resource.k9.sak.ansvarlig_saksbehandler"),
    BEHANDLING_STATUS("no.nav.abac.attributter.resource.k9.sak.behandlingsstatus"),
    FAGSAK_STATUS("no.nav.abac.attributter.resource.k9.sak.saksstatus"),
    SAKSNUMMER("no.nav.abac.attributter.resource.k9.saksnr"),
    YTELSESBEHANDLING_UUID("no.nav.abac.attributter.resource.k9.ytelsesbehandlingUuid");

    private final String key;

    private K9DataKeys(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }
}
