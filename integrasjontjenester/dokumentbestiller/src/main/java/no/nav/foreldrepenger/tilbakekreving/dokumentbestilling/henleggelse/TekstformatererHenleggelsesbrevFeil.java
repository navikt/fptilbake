package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.henleggelse;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface TekstformatererHenleggelsesbrevFeil extends DeklarerteFeil {
    TekstformatererHenleggelsesbrevFeil FACTORY = FeilFactory.create(TekstformatererHenleggelsesbrevFeil.class);

    @TekniskFeil(feilkode = "FPT-110801", feilmelding = "Feilet ved tekstgenerering til brev", logLevel = LogLevel.WARN)
    Feil feilVedTekstgenerering(Exception e);
}
