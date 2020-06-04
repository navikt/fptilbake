package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface EksternDataForBrevFeil extends DeklarerteFeil {

    EksternDataForBrevFeil FACTORY = FeilFactory.create(EksternDataForBrevFeil.class);

    @TekniskFeil(feilkode = "FPT-089912", feilmelding = "Fant ikke person med aktørId %s i tps", logLevel = LogLevel.WARN)
    Feil fantIkkeAdresseForAktørId(String aktørId);

    //FIXME k9-tilbake ikke hardkode fpsak i meldingen
    @TekniskFeil(feilkode = "FPT-841932", feilmelding = "Fant ikke behandling med saksnummer %s i fpsak", logLevel = LogLevel.WARN)
    Feil fantIkkeYtelesbehandlingIFagsystemet(String saksnummer);

    //FIXME k9-tilbake ikke hardkode fpoppdrag i meldingen, og kan ikke bruke eksternBehandlingId
    @TekniskFeil(feilkode = "FPT-748279", feilmelding = "Fant ikke behandling med behandlingId %s fpoppdrag", logLevel = LogLevel.WARN)
    Feil fantIkkeYtelesbehandlingISimuleringsapplikasjonen(Long behandlingId);
}
