package no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagFeil;

@Entity(name = "KravStatusKode")
@DiscriminatorValue(KravStatusKode.DISCRIMINATOR)
public class KravStatusKode extends Kodeliste {

    public static final String DISCRIMINATOR = "KRAV_STATUS_KODE";

    public static final KravStatusKode ANNULERT = new KravStatusKode("ANNU");
    public static final KravStatusKode ANNULLERT_OMG = new KravStatusKode("ANOM");
    public static final KravStatusKode AVSLUTTET = new KravStatusKode("AVSL");
    public static final KravStatusKode BEHANDLET = new KravStatusKode("BEHA");
    public static final KravStatusKode ENDRET = new KravStatusKode("ENDR");
    public static final KravStatusKode FEIL = new KravStatusKode("FEIL");
    public static final KravStatusKode MANUELL = new KravStatusKode("MANU");
    public static final KravStatusKode NYTT = new KravStatusKode("NY");
    public static final KravStatusKode SPERRET = new KravStatusKode("SPER");

    private static Map<String, KravStatusKode> kravStatusKodeMap = new HashMap<>();

    static {
        kravStatusKodeMap.put(ANNULERT.getKode(), ANNULERT);
        kravStatusKodeMap.put(ANNULLERT_OMG.getKode(), ANNULLERT_OMG);
        kravStatusKodeMap.put(AVSLUTTET.getKode(), AVSLUTTET);
        kravStatusKodeMap.put(BEHANDLET.getKode(), BEHANDLET);
        kravStatusKodeMap.put(ENDRET.getKode(), ENDRET);
        kravStatusKodeMap.put(FEIL.getKode(), FEIL);
        kravStatusKodeMap.put(MANUELL.getKode(), MANUELL);
        kravStatusKodeMap.put(NYTT.getKode(), NYTT);
        kravStatusKodeMap.put(SPERRET.getKode(), SPERRET);
    }

    KravStatusKode() {
        // For hibernate
    }

    private KravStatusKode(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public static KravStatusKode fraKode(String kode) {
        if (kravStatusKodeMap.containsKey(kode)) {
            return kravStatusKodeMap.get(kode);
        }
        throw KravgrunnlagFeil.FEILFACTORY.ugyldigKravStatusKode(kode).toException();
    }

}
