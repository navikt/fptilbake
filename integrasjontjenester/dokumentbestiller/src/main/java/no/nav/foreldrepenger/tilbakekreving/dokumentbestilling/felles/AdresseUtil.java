package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles;

import java.util.ArrayList;
import java.util.List;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;

public class AdresseUtil {
    private static List<String> landskodeNorge = new ArrayList<>();
    static {
        landskodeNorge.add("NOR");
        landskodeNorge.add("NO");
    }
    private AdresseUtil() {
        //indrer instansiering
    }

    public static boolean erNorskAdresse(Adresseinfo adresse) {
        return adresse.getLand() != null && landskodeNorge.contains(adresse.getLand()) &&
            adresse.getPostNr() != null;
    }

}
