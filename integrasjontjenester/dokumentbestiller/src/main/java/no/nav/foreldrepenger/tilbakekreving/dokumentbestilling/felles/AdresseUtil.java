package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;

public class AdresseUtil {
    private AdresseUtil() {
        //indrer instansiering
    }

    public static boolean erNorskAdresse(Adresseinfo adresse) {
        String landskodeNorge = "NOR";
        return adresse.getLand() != null && landskodeNorge.equalsIgnoreCase(adresse.getLand()) &&
            adresse.getPostNr() != null;
    }

}
