package no.nav.foreldrepenger.tilbakekreving.fplos.klient.task;

import static no.nav.vedtak.feil.LogLevel.WARN;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface FplosPubliserEventTaskFeil extends DeklarerteFeil {
    FplosPubliserEventTaskFeil FACTORY = FeilFactory.create(FplosPubliserEventTaskFeil.class);

    @TekniskFeil(feilkode = "FPT-770744", feilmelding = "Publisering av FPLOS event=%s feilet med exception %s", logLevel = WARN)
    Feil kanIkkePublisereFplosEventTilKafka(String eventName, Exception e);
}
