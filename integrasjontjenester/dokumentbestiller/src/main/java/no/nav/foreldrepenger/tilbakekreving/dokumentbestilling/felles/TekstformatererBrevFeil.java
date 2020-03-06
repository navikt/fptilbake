package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface TekstformatererBrevFeil extends DeklarerteFeil {
    TekstformatererBrevFeil FACTORY = FeilFactory.create(TekstformatererBrevFeil.class);

    @TekniskFeil(feilkode = "FPT-110800", feilmelding = "Feilet ved tekstgenerering til brev", logLevel = LogLevel.WARN)
    Feil feilVedTekstgenerering(Exception e);
}
