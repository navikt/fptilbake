package no.nav.foreldrepenger.tilbakekreving.behandling;

import static no.nav.vedtak.feil.LogLevel.WARN;

import java.util.UUID;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.FunksjonellFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface BehandlingFeil extends DeklarerteFeil {
    BehandlingFeil FACTORY = FeilFactory.create(BehandlingFeil.class);

    @TekniskFeil(feilkode = "FPT-7428492", feilmelding = "Fant ikke person med aktørId", logLevel = LogLevel.WARN)
    Feil fantIkkePersonMedAktørId();

    @FunksjonellFeil(feilkode = "FPT-185316", feilmelding = "Oppgitt saksnummer {%s} er allerede knyttet til en annen bruker", løsningsforslag = "Sjekk at saksnummer og fødselsnummer er riktig", logLevel = LogLevel.WARN)
    Feil saksnummerKnyttetTilAnnenBruker(Saksnummer saksnummer);

    @FunksjonellFeil(feilkode = "FPT-128935", feilmelding = "Behandlingen er endret av en annen saksbehandler, eller har blitt oppdatert med ny informasjon av systemet.", løsningsforslag = "Last inn behandlingen på nytt.", logLevel = LogLevel.WARN)
    Feil endringerHarForekommetPåSøknaden();

    @TekniskFeil(feilkode = "FPT-7428494", feilmelding = "Fant ikke person med fnr", logLevel = LogLevel.WARN)
    Feil fantIkkePersonIdentMedFnr();

    @TekniskFeil(feilkode = "FPT-7428495", feilmelding = "Fant ikke behandling med behandlingId %s and henvisning %s", logLevel = LogLevel.WARN)
    Feil fantIkkeBehandlingMedHenvisning(long behandlingId, Henvisning henvisning);

    @FunksjonellFeil(feilkode = "FPT-992332", feilmelding = "BehandlingId %s er ikke satt på vent, og ventefrist kan derfor ikke oppdateres", løsningsforslag = "Forsett saksbehandlingen", logLevel = WARN)
    Feil kanIkkeEndreVentefristForBehandlingIkkePaVent(Long behandlingId);

    @FunksjonellFeil(feilkode = "FPT-663486", feilmelding = "saksnummer %s oppfyller ikke kravene for tilbakekreving", løsningsforslag = "", logLevel = LogLevel.WARN)
    Feil kanIkkeOppretteTilbakekrevingBehandling(Saksnummer saksnummer);

    @FunksjonellFeil(feilkode = "FPT-663488", feilmelding = "tilbakekreving finnes allerede for eksternUuid %s ", løsningsforslag = "", logLevel = LogLevel.WARN)
    Feil kanIkkeOppretteTilbakekrevingBehandling(UUID eksternUuid);

    @FunksjonellFeil(feilkode = "FPT-663490", feilmelding = "Fant ingen tilbakekreving behandling for saksnummer %s ", løsningsforslag = "", logLevel = LogLevel.WARN)
    Feil fantIngenTilbakekrevingBehandlingForSaksnummer(Saksnummer saksnummer);

    @FunksjonellFeil(feilkode = "FPT-663491", feilmelding = "Det foreligger et feilutbetalt beløp eller opprettet automatisk før bestemte dager, kan ikke henlegges behandling %s", løsningsforslag = "", logLevel = LogLevel.WARN)
    Feil kanIkkeHenleggeBehandling(Long behandlingId);

    @TekniskFeil(feilkode = "FPT-763492", feilmelding = "Behandling er ikke fattet ennå, kan ikke finne vedtak info %s", logLevel = LogLevel.WARN)
    Feil fantIkkeBehandlingsVedtakInfo(Long behandlingId);
}
