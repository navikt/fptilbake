package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface TekstformatererVarselbrevFeil extends DeklarerteFeil {
    TekstformatererVarselbrevFeil FACTORY = FeilFactory.create(TekstformatererVarselbrevFeil.class);

    @TekniskFeil(feilkode = "FPT-110800", feilmelding = "Feilet ved tekstgenerering til brev", logLevel = LogLevel.WARN)
    Feil feilVedTekstgenerering(Exception e);
}
