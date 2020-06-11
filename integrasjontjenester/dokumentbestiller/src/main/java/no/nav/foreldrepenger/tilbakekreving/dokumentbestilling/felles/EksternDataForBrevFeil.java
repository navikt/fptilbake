package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface EksternDataForBrevFeil extends DeklarerteFeil {

    EksternDataForBrevFeil FACTORY = FeilFactory.create(EksternDataForBrevFeil.class);

    @TekniskFeil(feilkode = "FPT-089912", feilmelding = "Fant ikke person med aktørId %s i tps", logLevel = LogLevel.WARN)
    Feil fantIkkePersoniTPS(String aktørId);
}
