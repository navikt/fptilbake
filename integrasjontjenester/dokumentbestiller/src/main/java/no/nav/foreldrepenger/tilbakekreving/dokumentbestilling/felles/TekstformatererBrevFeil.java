package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles;

import no.nav.vedtak.exception.TekniskException;

public class TekstformatererBrevFeil {

    public static TekniskException feilVedTekstgenerering(Exception e) {
        return new TekniskException("FPT-110800", "Feilet ved tekstgenerering til brev");
    }
}
