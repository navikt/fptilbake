package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles;

import java.util.Optional;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.BrevMetadata;

public class BrevMottakerUtil {

    BrevMottakerUtil() {
        //privat konstruktor
    }

    public static String getAnnenMottakerNavn(BrevMetadata brevMetadata) {
        String mottakerNavn = brevMetadata.getMottakerAdresse().getMottakerNavn();
        String brukerNavn = brevMetadata.getSakspartNavn();
        String vergeNavn = brevMetadata.getVergeNavn();

        String annenMottakerNavn = "";
        if (mottakerNavn.equalsIgnoreCase(brukerNavn)) {
            annenMottakerNavn = vergeNavn;
        } else if (mottakerNavn.contains(vergeNavn)) {
            annenMottakerNavn = brukerNavn;
        }
        return annenMottakerNavn;
    }

    public static String getVergeNavn(Optional<VergeEntitet> vergeEntitet, Adresseinfo adresseinfo) {
        String vergeNavn = "";
        if (vergeEntitet.isPresent()) {
            VergeEntitet entitet = vergeEntitet.get();
            if (VergeType.ADVOKAT.equals(entitet.getVergeType())) {
                vergeNavn = adresseinfo.getMottakerNavn().replaceAll("\n",""); // Når verge er advokat, viser vi verge navn som "Virksomhet navn c/o verge navn"
            } else {
                vergeNavn = entitet.getNavn();
            }
        }
        return vergeNavn;
    }

}
