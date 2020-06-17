package no.nav.foreldrepenger.tilbakekreving.fpsak.klient;

import java.util.UUID;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;

public interface FpsakKlientFeil extends DeklarerteFeil {

    FpsakKlientFeil FACTORY = FeilFactory.create(FpsakKlientFeil.class);

    @IntegrasjonFeil(feilkode = "FPT-7428496", feilmelding = "Fant ingen ekstern behandling i Fpsak for Uuid %s", logLevel = LogLevel.WARN)
    Feil fantIkkeEksternBehandlingForUuid(String uuId);

    @IntegrasjonFeil(feilkode = "FPT-748279", feilmelding = "Fant ikke behandling med behandlingId %s fpoppdrag", logLevel = LogLevel.WARN)
    Feil fantIkkeYtelesbehandlingISimuleringsapplikasjonen(Long behandlingId);

    @IntegrasjonFeil(feilkode = "FPT-841932", feilmelding = "Fant ikke behandling med behandingUuid %s i fpsak", logLevel = LogLevel.WARN)
    Feil fantIkkeYtelesbehandlingIFagsystemet(UUID behandlingUuid);

}
