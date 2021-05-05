package no.nav.foreldrepenger.tilbakekreving.behandling;

import java.util.UUID;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.exception.TekniskException;

public class BehandlingFeil  {



    public static FunksjonellException saksnummerKnyttetTilAnnenBruker(Saksnummer saksnummer) {
        return new FunksjonellException("FPT-185316", String.format("Oppgitt saksnummer {%s} er allerede knyttet til en annen bruker", saksnummer), "Sjekk at saksnummer og fødselsnummer er riktig");
    }

    public static FunksjonellException  endringerHarForekommetPåSøknaden() {
        return new FunksjonellException("FPT-128935", "Behandlingen er endret av en annen saksbehandler, eller har blitt oppdatert med ny informasjon av systemet.", "Last inn behandlingen på nytt.");
    }

    public static TekniskException fantIkkePersonIdentMedFnr() {
        return new TekniskException("FPT-7428494", "Fant ikke person med fnr");
    }

    public static TekniskException fantIkkeBehandlingMedHenvisning(long behandlingId, Henvisning henvisning) {
        return new TekniskException("FPT-7428495", String.format("Fant ikke behandling med behandlingId %s and henvisning %s", behandlingId, henvisning));
    }

    public static FunksjonellException  kanIkkeEndreVentefristForBehandlingIkkePaVent(Long behandlingId) {
        return new FunksjonellException("FPT-992332", String.format("BehandlingId %s er ikke satt på vent, og ventefrist kan derfor ikke oppdateres", behandlingId), "Forsett saksbehandlingen");
    }

    public static FunksjonellException  kanIkkeOppretteTilbakekrevingBehandling(Saksnummer saksnummer) {
        return new FunksjonellException("FPT-663486", String.format("saksnummer %s oppfyller ikke kravene for tilbakekreving", saksnummer), "");
    }

    public static FunksjonellException  kanIkkeOppretteTilbakekrevingBehandling(UUID eksternUuid) {
        return new FunksjonellException("FPT-663488", String.format("tilbakekreving finnes allerede for eksternUuid %s ", eksternUuid), "");
    }

    public static FunksjonellException  fantIngenTilbakekrevingBehandlingForSaksnummer(Saksnummer saksnummer) {
        return new FunksjonellException("FPT-663490", String.format("Fant ingen tilbakekreving behandling for saksnummer %s ", saksnummer), "");
    }

    public static FunksjonellException  kanIkkeHenleggeBehandling(Long behandlingId) {
        return new FunksjonellException("FPT-663491", String.format("Det foreligger et feilutbetalt beløp eller opprettet automatisk før bestemte dager, kan ikke henlegges behandling %s", behandlingId), "");
    }

    public static TekniskException fantIkkeBehandlingsVedtakInfo(Long behandlingId) {
        return new TekniskException("FPT-763492", String.format("Behandling er ikke fattet ennå, kan ikke finne vedtak info %s", behandlingId));
    }
}
