package no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface FagsakFeil extends DeklarerteFeil {

    FagsakFeil FACTORY = FeilFactory.create(FagsakFeil.class);

    @TekniskFeil(feilkode = "FPT-429883", feilmelding = "Det var flere enn en Fagsak for saksnummer: %s", logLevel = LogLevel.WARN)
    Feil flereEnnEnFagsakForSaksnummer(Saksnummer saksnummer);

    @TekniskFeil(feilkode = "FPT-429884", feilmelding = "Fant ikke fagsak med saksnummer: %s", logLevel = LogLevel.WARN)
    Feil fantIkkeFagsakForSaksnummer(Saksnummer saksnummer);
}
