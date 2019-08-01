package no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagFeil;

@Entity(name = "GjelderType")
@DiscriminatorValue(GjelderType.DISCRIMINATOR)
public class GjelderType extends Kodeliste {

    public static final String DISCRIMINATOR = "GJELDER_TYPE";

    public static final GjelderType PERSON = new GjelderType("PERSON");
    public static final GjelderType ORGANISASJON = new GjelderType("ORGANISASJON");
    public static final GjelderType SAMHANDLER = new GjelderType("SAMHANDLER");
    public static final GjelderType APPBRUKER = new GjelderType("APPBRUKER");

    private static Map<String, GjelderType> gjelderTypeMap = new HashMap<>();

    static {
        gjelderTypeMap.put(PERSON.getKode(), PERSON);
        gjelderTypeMap.put(ORGANISASJON.getKode(), ORGANISASJON);
        gjelderTypeMap.put(SAMHANDLER.getKode(), SAMHANDLER);
        gjelderTypeMap.put(APPBRUKER.getKode(), APPBRUKER);
    }

    GjelderType() {
        // For hibernate
    }

    private GjelderType(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public static GjelderType fraKode(String kode) {
        if (gjelderTypeMap.containsKey(kode)) {
            return gjelderTypeMap.get(kode);
        }
        throw KravgrunnlagFeil.FEILFACTORY.ugyldigGjelderType(kode).toException();
    }

}
