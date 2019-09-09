package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.integrasjon.dokument.felles.SpraakkodeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodelisteNavnI18N;

public class BrevSpråkUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrevSpråkUtil.class);
    private static final Språkkode DEFAULT_SPRÅKKODE = Språkkode.nb;

    private BrevSpråkUtil() {
        // gjem implisitt konstruktør
    }

    public static SpraakkodeType mapSpråkkode(Språkkode språkkode) {
        return SpraakkodeType.fromValue(språkkode.getKode());
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



}
