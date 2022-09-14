package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac;

import no.nav.vedtak.sikkerhet.abac.AbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

public interface AppAbacAttributtType {

    //TODO k9tilbake, vurder å inline denne klassen

    AbacAttributtType AKSJONSPUNKT_KODE = StandardAbacAttributtType.AKSJONSPUNKT_KODE;
    AbacAttributtType AKTØR_ID = StandardAbacAttributtType.AKTØR_ID;
    AbacAttributtType BEHANDLING_ID = StandardAbacAttributtType.BEHANDLING_ID;
    AbacAttributtType BEHANDLING_UUID = StandardAbacAttributtType.BEHANDLING_UUID;
    AbacAttributtType FNR = StandardAbacAttributtType.FNR;
    AbacAttributtType SAKSNUMMER = StandardAbacAttributtType.SAKSNUMMER;

}
