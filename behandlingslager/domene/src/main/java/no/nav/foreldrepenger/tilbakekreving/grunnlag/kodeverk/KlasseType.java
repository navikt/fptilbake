package no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagFeil;

@Entity(name = "KlasseType")
@DiscriminatorValue(KlasseType.DISCRIMINATOR)
public class KlasseType extends Kodeliste {

    public static final String DISCRIMINATOR = "KLASSE_TYPE";

    public static final KlasseType FEIL = new KlasseType("FEIL");
    public static final KlasseType JUST = new KlasseType("JUST");
    public static final KlasseType SKAT = new KlasseType("SKAT");
    public static final KlasseType TREK = new KlasseType("TREK");
    public static final KlasseType YTEL = new KlasseType("YTEL");

    private static Map<String, KlasseType> klasseTypeMap = new HashMap<>();

    static {
        klasseTypeMap.put(FEIL.getKode(), FEIL);
        klasseTypeMap.put(JUST.getKode(), JUST);
        klasseTypeMap.put(SKAT.getKode(), SKAT);
        klasseTypeMap.put(TREK.getKode(), TREK);
        klasseTypeMap.put(YTEL.getKode(), YTEL);
    }

    KlasseType() {
        // For hibernate
    }

    private KlasseType(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public static KlasseType fraKode(String kode) {
        if (klasseTypeMap.containsKey(kode)) {
            return klasseTypeMap.get(kode);
        }
        throw KravgrunnlagFeil.FEILFACTORY.ugyldigKlasseType(kode).toException();
    }

}
