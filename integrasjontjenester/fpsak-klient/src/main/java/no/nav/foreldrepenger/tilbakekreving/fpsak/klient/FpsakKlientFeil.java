package no.nav.foreldrepenger.tilbakekreving.fpsak.klient;

import java.io.IOException;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface FpsakKlientFeil extends DeklarerteFeil {

    FpsakKlientFeil FACTORY = FeilFactory.create(FpsakKlientFeil.class);

    @TekniskFeil(feilkode = "FPT-532524", feilmelding = "Å lese repons feiler for saksnummer:%s med feilmelding:%s", logLevel = LogLevel.WARN)
    Feil lesResponsFeil(String saksnummer, IOException e);

    @TekniskFeil(feilkode = "FPT-7428496", feilmelding = "Fant ingen ekstern behandling i Fpsak for Uuid %s", logLevel = LogLevel.WARN)
    Feil fantIkkeEksternBehandlingForUuid(String uuId);

}
