package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles;

import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.BrevMetadata;

public class BrevMottakerUtil {

    public static String getAnnenMottakerNavn(BrevMetadata brevMetadata){
        String mottakerNavn = brevMetadata.getMottakerAdresse().getMottakerNavn();
        String brukerNavn = brevMetadata.getSakspartNavn();
        return mottakerNavn.equalsIgnoreCase(brukerNavn) ? brevMetadata.getVergeNavn() : brukerNavn;
    }
}
