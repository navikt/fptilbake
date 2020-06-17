package no.nav.foreldrepenger.tilbakekreving.k9sak.klient;

import java.io.IOException;
import java.util.UUID;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;

public interface K9sakKlientFeil extends DeklarerteFeil {

    K9sakKlientFeil FACTORY = FeilFactory.create(K9sakKlientFeil.class);

    @IntegrasjonFeil(feilkode = "FPT-532525", feilmelding = "Å lese repons feiler for saksnummer:%s med feilmelding:%s", logLevel = LogLevel.WARN)
    Feil lesResponsFeil(String saksnummer, IOException e);

    @IntegrasjonFeil(feilkode = "FPT-7428497", feilmelding = "Fant ingen ekstern behandling i K9sak for Uuid %s", logLevel = LogLevel.WARN)
    Feil fantIkkeEksternBehandlingForUuid(String uuId);

    @IntegrasjonFeil(feilkode = "FPT-748280", feilmelding = "Fant ikke behandling med behandlingId %s k9-oppdrag", logLevel = LogLevel.WARN)
    Feil fantIkkeYtelesbehandlingISimuleringsapplikasjonen(String behandlingId);

    @IntegrasjonFeil(feilkode = "FPT-841933", feilmelding = "Fant ikke behandling med behandingUuid %s i k9-sak", logLevel = LogLevel.WARN)
    Feil fantIkkeYtelesbehandlingIFagsystemet(UUID behandlingUuid);
}
