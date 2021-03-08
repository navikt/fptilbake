package no.nav.foreldrepenger.tilbakekreving.web.app.selftest;

import static no.nav.vedtak.feil.LogLevel.ERROR;
import static no.nav.vedtak.feil.LogLevel.WARN;

import java.io.IOException;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public class SelftestFeil {

    static TekniskException klarteIkkeÅLeseBuildTimePropertiesFil(IOException e) {
        return new TekniskException("FPT-635121", "Klarte ikke å lese build time properties fil", e);
    }

    static TekniskException dupliserteSelftestNavn(String name) {
        return new TekniskException("FPT-287026", String.format("Dupliserte selftest navn %s", name));
    }

    static TekniskException uventetSelftestFeil(IOException e) {
        return new TekniskException("FPT-409676", "Uventet feil", e);
    }

    static TekniskException kritiskSelftestFeilet(String description, String endpoint, String responseTime, String message) {
        return new TekniskException("FPT-932415", String.format("Selftest ERROR: %s. Endpoint: %s. Responstid: %s. Feilmelding: %s.", description, endpoint, responseTime, message));
    }

    static TekniskException ikkeKritiskSelftestFeilet(String description, String endpoint, String responseTime, String message) {
        return new TekniskException("FPT-984256", String.format("Selftest ERROR: %s. Endpoint: %s. Responstid: %s. Feilmelding: %s.", description, endpoint, responseTime, message));
    }
}
