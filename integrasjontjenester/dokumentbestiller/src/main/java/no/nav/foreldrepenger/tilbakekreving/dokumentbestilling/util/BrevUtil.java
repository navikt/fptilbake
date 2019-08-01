package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.util;

import java.sql.Date;
import java.text.DateFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.integrasjon.dokument.felles.SpraakkodeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodelisteNavnI18N;

public class BrevUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrevUtil.class);

    private static final String LANDSKODE_NORGE = "NOR";

    private static final Språkkode DEFAULT_SPRÅKKODE = Språkkode.nb;

    private BrevUtil() {
        // gjem implisitt konstruktør
    }

    public static boolean erNorskAdresse(Adresseinfo adresse) {
        return adresse.getLand() != null && LANDSKODE_NORGE.equalsIgnoreCase(adresse.getLand()) &&
            adresse.getPostNr() != null;
    }

    public static SpraakkodeType mapSpråkkode(Språkkode språkkode) {
        return SpraakkodeType.fromValue(språkkode.getKode());
    }

    public static String konverterFraLocaldateTilTekst(LocalDate dato) {
        DateFormat dateInstance = DateFormat.getDateInstance(1, new Locale("no"));
        return dateInstance.format(Date.valueOf(dato));
    }

    public static String finnLandnavnPåSpråk(List<KodelisteNavnI18N> landPåUlikeSpråk, Språkkode språkkode) {
        for (KodelisteNavnI18N kodelisteNavnI18N : landPåUlikeSpråk) {
            if (kodelisteNavnI18N.getSpråk().equals(språkkode.getKode())) {
                return kodelisteNavnI18N.getNavn();
            }
        }
        return null;
    }

    public static String finnFagsaktypenavnPåAngittSpråk(List<KodelisteNavnI18N> kodelisteNavnI18NList, Språkkode språkkode) {
        KodelisteNavnI18N fagsaknavn = kodelisteNavnI18NList.stream()
            .filter(fagsaktypenavn -> fagsaktypenavn.getSpråk().equals(språkkode.getKode()))
            .findFirst()
            .orElse(finnFagsaknavnPåDefaultSpråkvalg(kodelisteNavnI18NList));

        return fagsaknavn.getNavn()!= null ? fagsaknavn.getNavn().toLowerCase() : null;
    }

    private static KodelisteNavnI18N finnFagsaknavnPåDefaultSpråkvalg(List<KodelisteNavnI18N> kodelisteNavnI18NList) {
        Optional<KodelisteNavnI18N> fagsaktypeNavn = kodelisteNavnI18NList.stream()
            .filter(fagsaktypenavn -> DEFAULT_SPRÅKKODE.getKode().equals(fagsaktypenavn.getSpråk()))
            .findFirst();

        if (!fagsaktypeNavn.isPresent()) {
            LOGGER.warn("Kunne ikke finne fagsaktype på {}" , DEFAULT_SPRÅKKODE.getKode());
        }
        return fagsaktypeNavn.orElse(null);
    }

    public static String fjernNamespaceFra(String xml) {
        return xml.replaceAll("(<\\?[^<]*\\?>)?", ""). /* remove preamble */
            replaceAll(" xmlns.*?(\"|\').*?(\"|\')", "") /* remove xmlns declaration */
            .replaceAll("(<)(\\w+:)(.*?>)", "$1$3") /* remove opening tag prefix */
            .replaceAll("(</)(\\w+:)(.*?>)", "$1$3"); /* remove closing tags prefix */
    }

}
