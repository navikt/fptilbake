package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac;

import no.nav.vedtak.sikkerhet.abac.AbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

public class AppAbacAttributtType {

    //TODO k9tilbake, vurder å inline denne klassen

    public static final AbacAttributtType AKTØR_ID = StandardAbacAttributtType.AKTØR_ID;
    public static final AbacAttributtType BEHANDLING_ID = StandardAbacAttributtType.BEHANDLING_ID;
    public static final AbacAttributtType BEHANDLING_UUID = StandardAbacAttributtType.BEHANDLING_UUID;
    public static final AbacAttributtType FNR = StandardAbacAttributtType.FNR;
    public static final AbacAttributtType SAKSNUMMER = StandardAbacAttributtType.SAKSNUMMER;

    private AppAbacAttributtType() {
    }
}
