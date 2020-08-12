package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles;

import java.util.List;

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
}
