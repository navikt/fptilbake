package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9;

import no.nav.vedtak.sikkerhet.abac.pdp.RessursDataValue;

public enum PipAksjonspunktType implements RessursDataValue {
    AUTOPUNKT("Auto"),
    MANUELL("Manuell"),
    OVERSTYRING("Overstyring"),
    SAKSBEHANDLEROVERSTYRING("Saksbehandleroverstyring"),
    ;

    private final String verdi;

    private PipAksjonspunktType(String verdi) {
        this.verdi = verdi;
    }

    public String getVerdi() {
        return this.verdi;
    }
}
