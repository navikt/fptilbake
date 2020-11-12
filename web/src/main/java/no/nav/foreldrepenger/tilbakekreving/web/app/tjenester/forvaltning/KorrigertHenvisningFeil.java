package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface KorrigertHenvisningFeil extends DeklarerteFeil {

    KorrigertHenvisningFeil FACTORY = FeilFactory.create(KorrigertHenvisningFeil.class);

    @TekniskFeil(feilkode = "FPT-7728492", feilmelding = "Fant ikke eksternBehandlingUuid %s i fagsystemet. Kan ikke korrigere henvisningen for behandling %s",
        logLevel = LogLevel.WARN)
    Feil fantIkkeEksternBehandlingIFagsystem(String eksternBehandlingUuid, long behandlingId);

    @TekniskFeil(feilkode = "FPT-7728493", feilmelding = "EksternBehandlingUuid %s har ikke samme saksnummer %s som behandling. Kan ikke korrigere henvisningen for behandling %s",
        logLevel = LogLevel.WARN)
    Feil harIkkeSammeSaksnummer(String eksternBehandlingUuid, Saksnummer saksnummer, long behandlingId);
}
